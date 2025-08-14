package com.radiant.application;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/internal/v1/application"})
@Api(
   tags = {"Version management operations"}
)
public class ApplicationController {
   @Autowired
   private ApplicationPropertyService applicationPropertyService;

   @GetMapping({"/id"})
   public String getId() {
      return this.applicationPropertyService.getAppInstanceID();
   }
}
