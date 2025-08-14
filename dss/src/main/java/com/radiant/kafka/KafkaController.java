package com.radiant.kafka;

import com.radiant.dto.NameId;
import com.radiant.kafka.service.KafkaService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Lazy
@RestController
@Profile({"!disable-kafka"})
@RequestMapping({"/api/internal/v1/kafka"})
@Api(
   tags = {"Kafka message sender"}
)
public class KafkaController {
   @Autowired
   private KafkaService service;

   @PostMapping({"/{topic}/message"})
   @PreAuthorize("hasAnyAuthority('DSS_SYSADMIN')")
   public void message(@RequestBody KafkaMessageRequest request) {
      this.service.sendMessage(request.getTopic(), new GddsCourtEvent(GddsCourtEventType.LIST_UPLOADED, (NameId)null));
   }
}
