package com.radiant.lifecycle;

import com.radiant.GddsApplication;
import io.swagger.annotations.Api;
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
public class GddsLifecycleController {
   @ResponseStatus(HttpStatus.NO_CONTENT)
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @PostMapping({"/restart"})
   public void restart() {
      GddsApplication.restart();
   }
}
