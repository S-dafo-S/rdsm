package com.radiant.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.DssApplication;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.build.service.BuildService;
import com.radiant.court.service.DssCourtService;
import com.radiant.exception.WrongUrlException;
import com.radiant.kafka.ConsumerDto;
import com.radiant.kafka.ConsumerRecord;
import com.radiant.kafka.ConsumerResultDto;
import com.radiant.kafka.GddsCourtEvent;
import com.radiant.kafka.GddsQueryEvent;
import com.radiant.kafka.NetworkDssUpdateEvent;
import com.radiant.kafka.ProducerRecord;
import com.radiant.kafka.TopicsDto;
import com.radiant.query.service.DssQueryService;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.validation.constraints.NotNull;
import org.apache.http.client.HttpClient;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Primary
@Lazy
@Service
@Profile({"!disable-kafka"})
@ParametersAreNonnullByDefault
public class RestKafkaServiceImpl implements KafkaService {
   private static final Logger LOG = LoggerFactory.getLogger(RestKafkaServiceImpl.class);
   public static final long INITIAL_INTERVAL = 2000L;
   public static final long MAX_INTERVAL = 3600000L;
   public static final int MAX_RETRIES = 3;
   public static final double RETRY_INTERVAL_MULTIPLIER = (double)10.0F;
   private static final int POLLING_TIMEOUT_MS = 15000;
   private static final int CONSUMER_INIT_DELAY = 5000;
   private static final String CONSUMERS_TEMPLATE = "%s/consumers/%s";
   private static final String CONSUMER_TEMPLATE = "/instances/%s";
   private static final String CONSUMER_SUBSCRIPTION_TEMPLATE = "/instances/%s/subscription";
   private static final String TOPICS_TEMPLATE = "%s/topics/%s";
   private final Map<String, RestTemplate> topicRestTemplates = new ConcurrentHashMap();
   ObjectMapper mapper = new ObjectMapper();
   @Value("${kafka.bootstrapAddress.override}")
   private String bootstrapAddressOverride;
   @Value("${kafka.dss.topic.court}")
   private String dssCourtTopic;
   @Value("${kafka.dss.topic.query}")
   private String dssQueryTopic;
   @Value("${kafka.topic.network-update}")
   private String networkUpdateDssTopic;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private DssCourtService dssCourtService;
   @Autowired
   private DssQueryService dssQueryService;
   @Autowired
   private RestTemplateBuilder restTemplateBuilder;
   @Autowired
   private BuildService buildService;
   @Autowired
   private RestTemplateBuilder pollingRestTemplateBuilder;
   private String consumerGroupUrl;
   private RestTemplate restTemplate;
   private String kafkaUrl;
   private String dssId;
   private String kafkaClusterUrl;
   @Autowired
   private RetryTemplate retryTemplate;
   private boolean listenersStarted = false;

   public <T> void sendMessage(String topic, T msg) {
      if (!this.isConnected()) {
         LOG.warn("Kafka connection isn't active while DSS isn't connected");
      } else if (this.kafkaUrl != null) {
         this.createTopic(topic);
         String topicUrl = String.format("%s/topics/%s", this.kafkaUrl, topic);
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/vnd.kafka.json.v2+json");
         headers.add("Accept", "application/vnd.kafka.v2+json");
         HttpEntity<ProducerRecord<T>> entity = new HttpEntity(new ProducerRecord((String)null, msg), headers);
         String value = "";

         try {
            value = this.mapper.writeValueAsString(entity);
         } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
         }

         LOG.info("send message {} {}", this.consumerGroupUrl, value);

         try {
            this.restTemplate.exchange(topicUrl + "/", HttpMethod.POST, entity, String.class, new Object[0]);
            LOG.info("Sent message=[{}] to {}", msg, topic);
         } catch (RestClientResponseException ex) {
            LOG.info("Unable to send message=[{}] to {} due to : {}", new Object[]{msg, topic, ex.getMessage()});
         }

      }
   }

   private void createTopic(String topicName) {
      String topicsUrl = String.format("%s/topics/%s", this.kafkaUrl, "");

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.add("Accept", "application/vnd.kafka.v2+json");
         ResponseEntity<List<String>> response1 = this.restTemplate.exchange(topicsUrl, HttpMethod.GET, new HttpEntity(headers), new ParameterizedTypeReference<List<String>>() {
         }, new Object[0]);
         List<String> topics = (List)response1.getBody();
         if (topics != null && topics.stream().anyMatch((t) -> t.equals(topicName))) {
            return;
         }

         LOG.info("topic {} doesn't exist", topicName);
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

      String topicsUpdateUrl = String.format("%s/topics/%s", this.kafkaClusterUrl, "");
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json");
      JSONObject createTopicJsonObject = new JSONObject();
      createTopicJsonObject.put("topic_name", topicName);
      HttpEntity<String> request = new HttpEntity(createTopicJsonObject.toString(), headers);

      try {
         this.restTemplate.exchange(topicsUpdateUrl + "/", HttpMethod.POST, request, String.class, new Object[0]);
         LOG.info("topic {} created", topicName);
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

   }

   public void creatListeners() {
      if (!this.isConnected()) {
         LOG.info("Kafka connection isn't active while DSS isn't connected");
      } else {
         this.kafkaUrl = this.getKafkaUrl();
         this.dssId = this.applicationPropertyService.getStringValue("dss_id");
         HttpClient httpClient = this.getHttpClient();
         this.restTemplate = this.restTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient)).build();

         try {
            ResponseEntity<String> response = this.restTemplate.getForEntity(this.kafkaUrl + "/v3/clusters", String.class, new Object[0]);
            JsonNode root = this.mapper.readTree((String)response.getBody());
            String clusterId = root.path("data").path(0).path("cluster_id").asText();
            this.kafkaClusterUrl = String.format("%s/v3/clusters/%s", this.kafkaUrl, clusterId);
         } catch (JsonProcessingException | ResourceAccessException e) {
            throw new RuntimeException(e);
         }

         LOG.info("Creating kafka listeners {} {}", this.kafkaUrl, this.dssId);
         this.createTopicConsumer(this.kafkaUrl, this.dssCourtTopic);
         this.createTopicConsumer(this.kafkaUrl, this.dssQueryTopic);
         this.createTopicConsumer(this.kafkaUrl, this.networkUpdateDssTopic);
         this.listenersStarted = true;
      }
   }

   private HttpClient getHttpClient() {
      SSLContext sslContext;
      try {
         sslContext = (new SSLContextBuilder()).loadTrustMaterial((KeyStore)null, (x509CertChain, authType) -> true).build();
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      } catch (KeyManagementException e) {
         throw new RuntimeException(e);
      } catch (KeyStoreException e) {
         throw new RuntimeException(e);
      }

      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)).build());
      return HttpClients.custom().setConnectionManager(connectionManager).build();
   }

   private void createTopicConsumer(String kafkaUrlArg, @NotNull String topic) {
      this.consumerGroupUrl = String.format("%s/consumers/%s", kafkaUrlArg, this.dssId);
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/vnd.kafka.v2+json");
      headers.add("Accept", "application/vnd.kafka.v2+json");
      ConsumerDto dto = new ConsumerDto();
      String consumerName = String.format("%s-%s", this.dssId, topic);
      dto.setName(consumerName);
      dto.setFormat("json");
      dto.setAutoOffsetReset("earliest");
      dto.setAutoCommitEnable("false");
      dto.setFetchMinBytes("10");
      dto.setConsumerRequestTimeoutMs(Integer.toString(15000));
      HttpEntity<ConsumerDto> entity = new HttpEntity(dto, headers);

      String value;
      try {
         value = this.mapper.writeValueAsString(entity);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }

      LOG.info("creating consumer {} {}", this.consumerGroupUrl, value);
      String consumerId = "";

      try {
         ResponseEntity<ConsumerResultDto> response1 = this.restTemplate.exchange(this.consumerGroupUrl + "/", HttpMethod.POST, entity, ConsumerResultDto.class, new Object[0]);
         ConsumerResultDto resultDto = (ConsumerResultDto)response1.getBody();
         consumerId = resultDto != null ? resultDto.getInstanceId() : "";
         LOG.info("consumer {} created", consumerId);
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

      TopicsDto topics = new TopicsDto();
      topics.setTopics(Collections.singletonList(topic));
      HttpEntity<TopicsDto> topicsEntity = new HttpEntity(topics, headers);
      String subscriptionUrl = String.format("/instances/%s/subscription", consumerId);

      try {
         this.restTemplate.exchange(this.consumerGroupUrl + subscriptionUrl, HttpMethod.POST, topicsEntity, String.class, new Object[0]);
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

      if (!this.topicRestTemplates.containsKey(topic)) {
         HttpClient httpClient = this.getHttpClient();
         RestTemplate restTemplatePolling = this.pollingRestTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient)).setConnectTimeout(Duration.ofMillis(30000L)).setReadTimeout(Duration.ofMillis(30000L)).build();
         this.topicRestTemplates.put(topic, restTemplatePolling);
      }

   }

   @Scheduled(
      fixedDelay = 1L
   )
   public void pollDssCourtTopicTask() {
      List<ConsumerRecord<String, GddsCourtEvent>> events = this.pollRecords(this.dssCourtTopic, new ParameterizedTypeReference<List<ConsumerRecord<String, GddsCourtEvent>>>() {
      });
      events.forEach((ev) -> this.processMessageWithCommit(ev, (value) -> this.dssCourtService.processCourtUpdateEvent(value)));
   }

   @Scheduled(
      fixedDelay = 1L
   )
   public void pollDssQueryTopicTask() {
      List<ConsumerRecord<String, GddsQueryEvent>> events = this.pollRecords(this.dssQueryTopic, new ParameterizedTypeReference<List<ConsumerRecord<String, GddsQueryEvent>>>() {
      });
      AtomicBoolean needRestartWrapped = new AtomicBoolean();

      try {
         for(ConsumerRecord<String, GddsQueryEvent> ev : events) {
            this.processMessageWithCommit(ev, (value) -> {
               boolean res = this.dssQueryService.processQueryUpdateEvent(value);
               if (res) {
                  needRestartWrapped.set(true);
               }

            });
         }
      } finally {
         if (needRestartWrapped.get()) {
            DssApplication.restart();
         }

      }

   }

   @Scheduled(
      fixedDelay = 1L
   )
   public void pollNetworkDssUpdateTopicTask() {
      List<ConsumerRecord<String, NetworkDssUpdateEvent>> events = this.pollRecords(this.networkUpdateDssTopic, new ParameterizedTypeReference<List<ConsumerRecord<String, NetworkDssUpdateEvent>>>() {
      });
      events.forEach((ev) -> this.processMessageWithCommit(ev, (value) -> this.buildService.processNetworkDssUpdateEvent(value)));
   }

   @Scheduled(
      fixedDelay = 30000L
   )
   public void monitorInitState() {
      LOG.trace("Check kafka init");
      if (!this.listenersStarted) {
         try {
            this.creatListeners();
           } catch (Exception e) {
              LOG.warn("Kafka connection failed");
           }

      }
   }

   private <T> List<ConsumerRecord<String, T>> pollRecords(String topic, ParameterizedTypeReference<List<ConsumerRecord<String, T>>> responseType) {
      if (this.topicRestTemplates.containsKey(topic)) {
         String consumerName = String.format("%s-%s", this.dssId, topic);
         String consumerUrl = String.format("/instances/%s", consumerName);
         HttpHeaders headers = new HttpHeaders();
         headers.add("Accept", "application/vnd.kafka.json.v2+json");
         HttpEntity<?> record = new HttpEntity(headers);

         try {
            ResponseEntity<List<ConsumerRecord<String, T>>> res = ((RestTemplate)this.topicRestTemplates.get(topic)).exchange(this.consumerGroupUrl + consumerUrl + String.format("/records?timeout=%s", 15000), HttpMethod.GET, record, responseType, new Object[0]);
            return (List)res.getBody();
         } catch (RestClientResponseException ex) {
            try {
               Thread.sleep(5000L);
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }

            if (HttpStatus.NOT_FOUND.value() == ex.getRawStatusCode()) {
               this.createTopicConsumer(this.kafkaUrl, topic);
            }

            LOG.warn(ex.getMessage());
         }
      }

      try {
         Thread.sleep(5000L);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }

      return Collections.emptyList();
   }

   @PreDestroy
   private void removeConsumers() {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/vnd.kafka.v2+json");
      HttpEntity<?> entity = new HttpEntity(headers);

      for(Map.Entry<String, RestTemplate> entry : this.topicRestTemplates.entrySet()) {
         String consumerName = String.format("%s-%s", this.dssId, entry.getKey());
         String consumerUrl = String.format("/instances/%s", consumerName);

         try {
            ResponseEntity<String> delRes = ((RestTemplate)entry.getValue()).exchange(this.consumerGroupUrl + consumerUrl, HttpMethod.DELETE, entity, String.class, new Object[0]);
            LOG.info("Deleted {} ({})", this.consumerGroupUrl + consumerUrl, delRes.getBody());
         } catch (RestClientResponseException ex) {
            LOG.warn("Tried to delete {}", this.consumerGroupUrl + consumerUrl);
            LOG.warn(ex.getMessage());
         }
      }

   }

   private String getKafkaUrl() {
      if (!this.bootstrapAddressOverride.isEmpty()) {
         return this.bootstrapAddressOverride;
      } else {
         String gddsUrl = this.applicationPropertyService.getStringValue("gdds_url");

         URI gddsUri;
         try {
            gddsUri = new URI(gddsUrl);
           } catch (URISyntaxException e) {
              throw new WrongUrlException(gddsUrl);
           }

           try {
              URI kafkaUri = new URI(gddsUri.getScheme(), gddsUri.getUserInfo(), gddsUri.getHost(), gddsUri.getPort(), "/kafka", gddsUri.getQuery(), gddsUri.getFragment());
              return kafkaUri.toString();
           } catch (URISyntaxException e) {
              throw new WrongUrlException(gddsUrl);
           }
      }
   }

   private <T> void acknowledge(ConsumerRecord<String, T> ev) {
      if (this.topicRestTemplates.containsKey(ev.getTopic())) {
         String consumerName = String.format("%s-%s", this.dssId, ev.getTopic());
         String consumerUrl = String.format("/instances/%s", consumerName);
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/vnd.kafka.v2+json");
         Offsets offsets = new Offsets();
         offsets.setOffsets(Collections.singletonList(RestKafkaServiceImpl.Offset.builder().topic(ev.getTopic()).partition(ev.getPartition()).offset(ev.getOffset()).build()));
         HttpEntity<Offsets> offsetsData = new HttpEntity(offsets, headers);

         try {
            ResponseEntity<String> res = ((RestTemplate)this.topicRestTemplates.get(ev.getTopic())).exchange(this.consumerGroupUrl + consumerUrl + "/offsets", HttpMethod.POST, offsetsData, String.class, new Object[0]);
            if (res.getStatusCode().equals(HttpStatus.OK)) {
               LOG.info("Consumed message has been committed");
            }
         } catch (RestClientResponseException ex) {
            LOG.warn(ex.getMessage());
         }
      }

   }

   private <T> void getLastCommitedOffset(ConsumerRecord<String, T> ev) {
      String consumerName = String.format("%s-%s", this.dssId, ev.getTopic());
      String consumerUrl = String.format("/instances/%s", consumerName);
      HttpHeaders headers1 = new HttpHeaders();
      headers1.add("Content-Type", "application/vnd.kafka.v2+json");
      Partitions partitions = new Partitions();
      partitions.setPartitions(Collections.singletonList(RestKafkaServiceImpl.Partition.builder().topic(ev.getTopic()).partition(ev.getPartition()).build()));
      HttpEntity<Partitions> partitionsData1 = new HttpEntity(partitions, headers1);

      try {
         ResponseEntity<String> res = this.restTemplate.exchange(this.consumerGroupUrl + consumerUrl + "/offsets", HttpMethod.GET, partitionsData1, String.class, new Object[0]);
         if (res.getStatusCode().equals(HttpStatus.OK)) {
            LOG.info("last comitted offset:  {}", res.getBody());
         }
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

   }

   private <T> void resetOffset(ConsumerRecord<String, T> ev) {
      String consumerName = String.format("%s-%s", this.dssId, ev.getTopic());
      String consumerUrl = String.format("/instances/%s", consumerName);
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/vnd.kafka.v2+json");
      Offsets offsets = new Offsets();
      offsets.setOffsets(Collections.singletonList(RestKafkaServiceImpl.Offset.builder().topic(ev.getTopic()).partition(ev.getPartition()).offset(ev.getOffset()).build()));
      HttpEntity<Offsets> offsetsData = new HttpEntity(offsets, headers);

      try {
         ResponseEntity<String> res = this.restTemplate.exchange(this.consumerGroupUrl + consumerUrl + "/positions", HttpMethod.POST, offsetsData, String.class, new Object[0]);
         LOG.info("Offset was reset to {}", ev.getOffset());
      } catch (RestClientResponseException ex) {
         LOG.warn(ex.getMessage());
      }

   }

   private <T> void processMessageWithCommit(ConsumerRecord<String, T> ev, Consumer<T> action) {
      this.retryTemplate.execute((retryContext) -> {
         action.accept(ev.getValue());
         this.acknowledge(ev);
         return true;
      });
   }

   private boolean isConnected() {
      String gddsUrl = this.applicationPropertyService.getStringValue("gdds_url");
      String accountId = this.applicationPropertyService.getStringValue("dss_id");
      return gddsUrl != null && accountId != null;
   }

   @ParametersAreNonnullByDefault
   private static class Offset implements Serializable {
      String topic;
      int partition;
      long offset;

      protected Offset(final OffsetBuilder<?, ?> b) {
         this.topic = b.topic;
         this.partition = b.partition;
         this.offset = b.offset;
      }

      public static OffsetBuilder<?, ?> builder() {
         return new OffsetBuilderImpl();
      }

      public String getTopic() {
         return this.topic;
      }

      public int getPartition() {
         return this.partition;
      }

      public long getOffset() {
         return this.offset;
      }

      public void setTopic(final String topic) {
         this.topic = topic;
      }

      public void setPartition(final int partition) {
         this.partition = partition;
      }

      public void setOffset(final long offset) {
         this.offset = offset;
      }

      public String toString() {
         return "RestKafkaServiceImpl.Offset(topic=" + this.getTopic() + ", partition=" + this.getPartition() + ", offset=" + this.getOffset() + ")";
      }

      public Offset() {
      }

      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Offset)) {
            return false;
         } else {
            Offset other = (Offset)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.getPartition() != other.getPartition()) {
               return false;
            } else if (this.getOffset() != other.getOffset()) {
               return false;
            } else {
               Object this$topic = this.getTopic();
               Object other$topic = other.getTopic();
               if (this$topic == null) {
                  if (other$topic != null) {
                     return false;
                  }
               } else if (!this$topic.equals(other$topic)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(final Object other) {
         return other instanceof Offset;
      }

      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getPartition();
         long $offset = this.getOffset();
         result = result * 59 + (int)($offset >>> 32 ^ $offset);
         Object $topic = this.getTopic();
         result = result * 59 + ($topic == null ? 43 : $topic.hashCode());
         return result;
      }

      public abstract static class OffsetBuilder<C extends Offset, B extends OffsetBuilder<C, B>> {
         private String topic;
         private int partition;
         private long offset;

         protected abstract B self();

         public abstract C build();

         public B topic(final String topic) {
            this.topic = topic;
            return (B)this.self();
         }

         public B partition(final int partition) {
            this.partition = partition;
            return (B)this.self();
         }

         public B offset(final long offset) {
            this.offset = offset;
            return (B)this.self();
         }

         public String toString() {
            return "RestKafkaServiceImpl.Offset.OffsetBuilder(topic=" + this.topic + ", partition=" + this.partition + ", offset=" + this.offset + ")";
         }
      }

      private static final class OffsetBuilderImpl extends OffsetBuilder<Offset, OffsetBuilderImpl> {
         private OffsetBuilderImpl() {
         }

         protected OffsetBuilderImpl self() {
            return this;
         }

         public Offset build() {
            return new Offset(this);
         }
      }
   }

   @ParametersAreNonnullByDefault
   private static class Partition implements Serializable {
      String topic;
      int partition;

      protected Partition(final PartitionBuilder<?, ?> b) {
         this.topic = b.topic;
         this.partition = b.partition;
      }

      public static PartitionBuilder<?, ?> builder() {
         return new PartitionBuilderImpl();
      }

      public String getTopic() {
         return this.topic;
      }

      public int getPartition() {
         return this.partition;
      }

      public void setTopic(final String topic) {
         this.topic = topic;
      }

      public void setPartition(final int partition) {
         this.partition = partition;
      }

      public String toString() {
         return "RestKafkaServiceImpl.Partition(topic=" + this.getTopic() + ", partition=" + this.getPartition() + ")";
      }

      public Partition() {
      }

      public abstract static class PartitionBuilder<C extends Partition, B extends PartitionBuilder<C, B>> {
         private String topic;
         private int partition;

         protected abstract B self();

         public abstract C build();

         public B topic(final String topic) {
            this.topic = topic;
            return (B)this.self();
         }

         public B partition(final int partition) {
            this.partition = partition;
            return (B)this.self();
         }

         public String toString() {
            return "RestKafkaServiceImpl.Partition.PartitionBuilder(topic=" + this.topic + ", partition=" + this.partition + ")";
         }
      }

      private static final class PartitionBuilderImpl extends PartitionBuilder<Partition, PartitionBuilderImpl> {
         private PartitionBuilderImpl() {
         }

         protected PartitionBuilderImpl self() {
            return this;
         }

         public Partition build() {
            return new Partition(this);
         }
      }
   }

   @ParametersAreNonnullByDefault
   private static class Offsets implements Serializable {
      List<Offset> offsets;

      public List<Offset> getOffsets() {
         return this.offsets;
      }

      public void setOffsets(final List<Offset> offsets) {
         this.offsets = offsets;
      }

      public String toString() {
         return "RestKafkaServiceImpl.Offsets(offsets=" + this.getOffsets() + ")";
      }

      public Offsets() {
      }
   }

   @ParametersAreNonnullByDefault
   private static class Partitions implements Serializable {
      List<Partition> partitions;

      public List<Partition> getPartitions() {
         return this.partitions;
      }

      public void setPartitions(final List<Partition> partitions) {
         this.partitions = partitions;
      }

      public String toString() {
         return "RestKafkaServiceImpl.Partitions(partitions=" + this.getPartitions() + ")";
      }

      public Partitions() {
      }
   }
}
