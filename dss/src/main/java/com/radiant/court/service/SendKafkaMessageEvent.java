package com.radiant.court.service;

import com.radiant.court.domain.dto.CourtDto;
import com.radiant.kafka.DssCourtHostEventType;
import java.util.List;

public class SendKafkaMessageEvent {
   private final DssCourtHostEventType type;
   private final List<CourtDto> courts;

   public SendKafkaMessageEvent(DssCourtHostEventType type, List<CourtDto> courts) {
      this.type = type;
      this.courts = courts;
   }

   public DssCourtHostEventType getType() {
      return this.type;
   }

   public List<CourtDto> getCourts() {
      return this.courts;
   }
}
