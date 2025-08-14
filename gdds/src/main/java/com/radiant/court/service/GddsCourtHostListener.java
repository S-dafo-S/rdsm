package com.radiant.court.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.kafka.DssCourtHostEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Profile({"!disable-kafka"})
public class GddsCourtHostListener {
   @Autowired
   private GddsCourtHostService gddsCourtHostService;
   private static final ObjectMapper MAPPER = new ObjectMapper();

   @KafkaListener(
      id = "gddsListener",
      topics = {"${kafka.gdds.topic.court}"},
      groupId = "${kafka.gdds.groupId}"
   )
   public void listen(@Payload String eventJson) {
      DssCourtHostEvent courtEvent;
      try {
         MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         courtEvent = (DssCourtHostEvent)MAPPER.readValue(eventJson, DssCourtHostEvent.class);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }

      this.gddsCourtHostService.processCourtHostUpdateEvent(courtEvent);
   }
}
