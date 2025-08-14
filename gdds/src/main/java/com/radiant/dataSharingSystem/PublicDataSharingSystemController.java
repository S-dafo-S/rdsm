package com.radiant.dataSharingSystem;

import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.connect.GddsDssConnectResponse;
import com.radiant.connect.GddsDssUpdateRequest;
import com.radiant.connect.GddsDssUpdateResponse;
import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/public/v1/dnode"})
@Api(
   tags = {"DSS management operations"}
)
public class PublicDataSharingSystemController {
   @Autowired
   private DataSharingSystemService dataSharingSystemService;

   @PostMapping({"/connect"})
   public GddsDssConnectResponse executeConnect(@RequestBody GddsDssConnectRequest connectRequest) {
      return this.dataSharingSystemService.connect(connectRequest);
   }

   @PostMapping({"/update"})
   public GddsDssUpdateResponse executeDssUpdate(@RequestBody GddsDssUpdateRequest updateRequest) {
      return this.dataSharingSystemService.updateFromDss(updateRequest);
   }

   @GetMapping({"/{id}/health-check"})
   public Boolean getDss(@PathVariable("id") Long id) {
      return this.dataSharingSystemService.healthCheck(id);
   }

   @PostMapping({"/cli-request"})
   public String cliRequest(@RequestBody CliRequest request) {
      return this.dataSharingSystemService.cliRequest(request);
   }
}
