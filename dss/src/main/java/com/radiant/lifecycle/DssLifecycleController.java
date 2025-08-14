package com.radiant.lifecycle;

import com.radiant.DssApplication;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"GDDS application lifecycle"}
)
@RestController
@RequestMapping({"/api/internal/v1/app"})
public class DssLifecycleController {
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;

   @ResponseStatus(HttpStatus.NO_CONTENT)
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   @PostMapping({"/restart"})
   public void restart() {
      this.serviceLogManagementService.log(LogLevel.INFO, "DSS app restarting...");
      DssApplication.restart();
   }
}
