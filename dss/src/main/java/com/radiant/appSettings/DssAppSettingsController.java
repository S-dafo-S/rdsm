package com.radiant.appSettings;

import com.radiant.appSettings.domain.TokenSettingsDto;
import com.radiant.appSettings.service.DssAppSettingsService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"DNode application settings operations"}
)
@RestController
@RequestMapping({"/api/internal/v1/settings/dnode", "/api/v1/settings/dnode"})
public class DssAppSettingsController {
   @Autowired
   private DssAppSettingsService appSettingsService;

   @GetMapping({"/token"})
   public TokenSettingsDto getTokenSettings() {
      return this.appSettingsService.getTokenSetting();
   }

   @PostMapping({"/token"})
   public TokenSettingsDto updateTokenSettings(@RequestBody TokenSettingsDto tokenSettings) {
      return this.appSettingsService.updateTokenSettings(tokenSettings);
   }
}
