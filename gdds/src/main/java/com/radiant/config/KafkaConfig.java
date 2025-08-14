package com.radiant.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Lazy
@EnableKafka
@Configuration
@Profile({"!disable-kafka"})
public class KafkaConfig {
   private static final long MAX_INTERVAL = 3600000L;
   private static final int MAX_RETRIES = 3;
   private static final double RETRY_INTERVAL_MULTIPLIER = (double)10.0F;
   @Value("${kafka.bootstrapAddress}")
   private String bootstrapAddress;
   @Value("${kafka.gdds.groupId}")
   private String groupId;

   @Bean
   public KafkaAdmin kafkaAdmin() {
      Map<String, Object> configs = new HashMap();
      configs.put("bootstrap.servers", this.bootstrapAddress);
      return new KafkaAdmin(configs);
   }

   @Bean
   public KafkaTemplate<String, Object> kafkaTemplate() {
      return new KafkaTemplate(this.producerFactory());
   }

   @Bean
   public ProducerFactory<String, Object> producerFactory() {
      Map<String, Object> props = new HashMap();
      props.put("bootstrap.servers", this.bootstrapAddress);
      props.put("key.serializer", StringSerializer.class);
      props.put("value.serializer", JsonSerializer.class);
      props.put("spring.json.type.mapping", "gddsCourtEvent:com.radiant.kafka.GddsCourtEvent");
      return new DefaultKafkaProducerFactory(props);
   }

   @Bean
   public ConsumerFactory<String, Object> consumerFactory() {
      Map<String, Object> props = new HashMap();
      props.put("bootstrap.servers", this.bootstrapAddress);
      props.put("group.id", this.groupId);
      props.put("enable.auto.commit", false);
      props.put("key.deserializer", StringDeserializer.class);
      props.put("value.deserializer", StringDeserializer.class);
      return new DefaultKafkaConsumerFactory(props);
   }

   @Bean
   public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
      ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory();
      factory.setConsumerFactory(this.consumerFactory());
      ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(3);
      backoff.setMaxInterval(3600000L);
      backoff.setMultiplier((double)10.0F);
      factory.setErrorHandler(new SeekToCurrentErrorHandler(backoff));
      return factory;
   }
}
