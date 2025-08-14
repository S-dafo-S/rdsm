package com.radiant.dataSharingSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.kafka.DssUpdateStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Profile({"!disable-kafka"})
public class GddsDssUpdateListener {
   private static final Logger LOG = LoggerFactory.getLogger(GddsDssUpdateListener.class);
   @Autowired
   private DataSharingSystemService dataSharingSystemService;
   private static final ObjectMapper MAPPER = new ObjectMapper();

   @KafkaListener(
      id = "gddsDssUpdateListener",
      topics = {"${kafka.topic.dss-update-status}"},
      groupId = "${kafka.gdds.groupId}"
   )
   public void listen(@Payload String eventJson) {
      LOG.info("Received in dss update status event: [{}]", eventJson);

      DssUpdateStatusEvent event;
      try {
         event = (DssUpdateStatusEvent)MAPPER.readValue(eventJson, DssUpdateStatusEvent.class);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }

      this.dataSharingSystemService.processDssUpdateStatusEvent(event);
   }
}
