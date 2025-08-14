package com.radiant.appSettings;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.build.ReleaseInfoEventService;
import com.radiant.build.domain.Upgrade;
import com.radiant.kafka.DssUpdateStatusEvent;
import com.radiant.kafka.service.KafkaService;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReleaseInfoEventServiceImpl implements ReleaseInfoEventService {
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Value("${kafka.topic.dss-update-status}")
   private String dssUpdateStatusTopic;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;

   public void reportUpdateStatus(Upgrade upgrade) {
      if (this.kafkaService != null) {
         this.kafkaService.sendMessage(this.dssUpdateStatusTopic, new DssUpdateStatusEvent(upgrade.getAction(), upgrade.getUuid(), upgrade.getVersion(), upgrade.getOldVersion(), upgrade.getDnodeAccountId() != null ? upgrade.getDnodeAccountId() : this.applicationPropertyService.getStringValue("dss_id"), upgrade.getStatus(), upgrade.getTimestamp(), upgrade.getSchedule()));
      }

   }
}
