package com.radiant.court.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SendKafkaMessageEventListener {
   private final DssCourtEventService dssCourtEventService;

   public SendKafkaMessageEventListener(DssCourtEventService dssCourtEventService) {
      this.dssCourtEventService = dssCourtEventService;
   }

   @TransactionalEventListener
   public void handleSendKafkaMessageEvent(SendKafkaMessageEvent sendKafkaMessageEvent) {
      sendKafkaMessageEvent.getCourts().forEach((courtDto) -> this.dssCourtEventService.sendKafkaMessage(sendKafkaMessageEvent.getType(), courtDto));
   }
}
