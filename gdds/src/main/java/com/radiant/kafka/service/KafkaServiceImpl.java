package com.radiant.kafka.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Lazy
@Service
@Profile({"!disable-kafka"})
@ParametersAreNonnullByDefault
public class KafkaServiceImpl implements KafkaService {
   private static final Logger LOG = LoggerFactory.getLogger(KafkaServiceImpl.class);
   @Autowired
   private KafkaTemplate<String, Object> kafkaTemplate;

   public <T> void sendMessage(final String topic, final T msg) {
      ListenableFuture<SendResult<String, Object>> future = this.kafkaTemplate.send(topic, msg);
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
