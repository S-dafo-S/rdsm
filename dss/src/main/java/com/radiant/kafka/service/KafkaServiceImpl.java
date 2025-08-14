package com.radiant.kafka.service;

import com.radiant.DssApplication;
import com.radiant.court.service.DssCourtService;
import com.radiant.gddsConnect.domain.dto.GddsConnectDto;
import com.radiant.gddsConnect.service.GddsConnectService;
import com.radiant.kafka.GddsCourtEvent;
import com.radiant.kafka.GddsQueryEvent;
import com.radiant.query.service.DssQueryService;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Lazy
@Service
@Profile({"!disable-kafka"})
@ParametersAreNonnullByDefault
public class KafkaServiceImpl implements KafkaService {
   private static final Logger LOG = LoggerFactory.getLogger(KafkaServiceImpl.class);
   private static final long MAX_INTERVAL = 3600000L;
   private static final int MAX_RETRIES = 3;
   private static final double RETRY_INTERVAL_MULTIPLIER = (double)10.0F;
   @Value("${kafka.bootstrapAddress.override}")
   private String bootstrapAddressOverride;
   @Value("${kafka.dss.topic.court}")
   private String dssCourtTopic;
   @Value("${kafka.dss.topic.query}")
   private String dssQueryTopic;
   @Autowired
   private GddsConnectService connectService;
   @Autowired
   private DssCourtService dssCourtService;
   @Autowired
   private DssQueryService dssQueryService;

   public <T> void sendMessage(final String topic, final T msg) {
      if (!this.connectService.isConnected()) {
         LOG.warn("Kafka connection isn't active while DSS isn't connected");
      } else {
         String kafkaUrl = this.getKafkaUrl();
         KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate(producerFactory(kafkaUrl));
         ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, msg);
         future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            public void onSuccess(@Nullable SendResult<String, Object> result) {
               KafkaServiceImpl.LOG.info("Sent message=[{}] to {}", msg, topic);
            }

            public void onFailure(Throwable ex) {
               KafkaServiceImpl.LOG.info("Unable to send message=[{}] to {} due to : {}", new Object[]{msg, topic, ex.getMessage()});
            }
         });
      }
   }

   public void creatListeners() {
      if (!this.connectService.isConnected()) {
         LOG.info("Kafka connection isn't active while DSS isn't connected");
      } else {
         GddsConnectDto connectInfo = this.connectService.getConnectInfo();
         String kafkaUrl = this.getKafkaUrl();
         String dssId = connectInfo.getAccountId();
         LOG.info("Creating kafka listeners {} {}", kafkaUrl, dssId);
         this.createListener(kafkaUrl, dssId, this.dssCourtTopic, new DssCourtTopicMessageListener());
         this.createListener(kafkaUrl, dssId, this.dssQueryTopic, new DssQueryTopicMessageListener());
      }
   }

   private <T> void createListener(String kafkaUrl, String dssId, String topic, MessageListener<String, T> listener) {
      ConsumerFactory<String, T> factory = consumerFactory(kafkaUrl, dssId);
      ContainerProperties containerProps = new ContainerProperties(new String[]{topic});
      containerProps.setMessageListener(listener);
      containerProps.setAckMode(AckMode.MANUAL_IMMEDIATE);
      KafkaMessageListenerContainer<String, T> container = new KafkaMessageListenerContainer(factory, containerProps);
      ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(3);
      backoff.setMaxInterval(3600000L);
      backoff.setMultiplier((double)10.0F);
      container.setErrorHandler(new SeekToCurrentErrorHandler(backoff));
      container.start();
   }

   private static ProducerFactory<String, Object> producerFactory(String kafkaUrl) {
      Map<String, Object> props = new HashMap();
      props.put("bootstrap.servers", kafkaUrl);
      props.put("key.serializer", StringSerializer.class);
      props.put("value.serializer", JsonSerializer.class);
      props.put("spring.json.type.mapping", "dssCourtHostEvent:com.radiant.kafka.DssCourtHostEvent");
      return new DefaultKafkaProducerFactory(props);
   }

   private static <T> ConsumerFactory<String, T> consumerFactory(String kafkaUrl, String dssId) {
      Map<String, Object> props = new HashMap();
      props.put("bootstrap.servers", kafkaUrl);
      props.put("group.id", dssId);
      props.put("enable.auto.commit", false);
      props.put("key.deserializer", StringDeserializer.class);
      props.put("value.deserializer", JsonDeserializer.class);
      props.put("spring.json.type.mapping", "gddsCourtEvent:com.radiant.kafka.GddsCourtEvent");
      return new DefaultKafkaConsumerFactory(props);
   }

   private String getKafkaUrl() {
      GddsConnectDto connectInfo = this.connectService.getConnectInfo();
      return !this.bootstrapAddressOverride.isEmpty() ? this.bootstrapAddressOverride : connectInfo.getGddsUrl().replaceFirst(":\\d+", ":29092");
   }

   private class DssCourtTopicMessageListener implements AcknowledgingMessageListener<String, GddsCourtEvent> {
      private DssCourtTopicMessageListener() {
      }

      public void onMessage(ConsumerRecord<String, GddsCourtEvent> data, @Nullable Acknowledgment acknowledgment) {
         KafkaServiceImpl.this.dssCourtService.processCourtUpdateEvent((GddsCourtEvent)data.value());
         if (acknowledgment != null) {
            acknowledgment.acknowledge();
         } else {
            KafkaServiceImpl.LOG.warn("No acknowledgment headers in {}", data);
         }

      }
   }

   private class DssQueryTopicMessageListener implements AcknowledgingMessageListener<String, GddsQueryEvent> {
      private DssQueryTopicMessageListener() {
      }

      public void onMessage(ConsumerRecord<String, GddsQueryEvent> data, @Nullable Acknowledgment acknowledgment) {
         boolean needRestart = KafkaServiceImpl.this.dssQueryService.processQueryUpdateEvent((GddsQueryEvent)data.value());
         if (acknowledgment != null) {
            acknowledgment.acknowledge();
         } else {
            KafkaServiceImpl.LOG.warn("No acknowledgment headers in {}", data);
         }

         if (needRestart) {
            DssApplication.restart();
         }

      }
   }
}
