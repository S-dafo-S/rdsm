package com.radiant.court.service;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.kafka.DssCourtHostEvent;
import com.radiant.kafka.DssCourtHostEventType;
import com.radiant.kafka.service.KafkaService;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ParametersAreNonnullByDefault
public class DssCourtEventServiceImpl implements DssCourtEventService {
   private static final Logger LOG = LoggerFactory.getLogger(DssCourtEventServiceImpl.class);
   @Value("${kafka.gdds.topic.court}")
   private String gddsCourtTopic;
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public void sendKafkaMessage(DssCourtHostEventType type, @Nullable CourtDto data) {
      if (this.kafkaService != null) {
         String dssId = this.applicationPropertyService.getStringValue("dss_id");
         this.kafkaService.sendMessage(this.gddsCourtTopic, new DssCourtHostEvent(dssId, type, data));
      } else {
         LOG.warn("Kafka isn't active");
      }

   }
}
