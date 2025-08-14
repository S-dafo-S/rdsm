package com.radiant.court.service;

import com.radiant.court.domain.dto.CourtDto;
import com.radiant.kafka.DssCourtHostEventType;
import javax.annotation.Nullable;

public interface DssCourtEventService {
   void sendKafkaMessage(DssCourtHostEventType type, @Nullable CourtDto data);
}
