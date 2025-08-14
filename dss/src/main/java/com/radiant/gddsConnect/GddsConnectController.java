package com.radiant.gddsConnect;

import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.gddsConnect.domain.dto.GddsConnectDto;
import com.radiant.gddsConnect.service.GddsConnectService;
import com.radiant.securityAnnotation.IsDataManager;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/internal/v1/qnode-connect"})
@Api(
   tags = {"Connect to QNode operations"}
)
public class GddsConnectController {
   @Autowired
   private GddsConnectService gddsConnectService;

   @GetMapping
   public GddsConnectDto getConnectInfo() {
      return this.gddsConnectService.getConnectInfo();
   }

   @IsDataManager
   @PostMapping({"/execute"})
   public GddsConnectDto executeConnect(@RequestBody GddsDssConnectRequest request) {
      return this.gddsConnectService.executeConnect(request);
   }
}
