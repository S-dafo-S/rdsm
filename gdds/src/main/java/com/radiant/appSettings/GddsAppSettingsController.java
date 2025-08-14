package com.radiant.appSettings;

import com.radiant.appSettings.domain.AccessLogSettingsDto;
import com.radiant.appSettings.domain.GddsAppSettingsDto;
import com.radiant.appSettings.domain.TokenSettingsDto;
import com.radiant.appSettings.service.GddsAppSettingsService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
   tags = {"GDDS application settings operations"}
)
@RestController
@RequestMapping({"/api/internal/v1/settings/qnode"})
public class GddsAppSettingsController {
   @Autowired
   private GddsAppSettingsService appSettingsService;

   @GetMapping
   public GddsAppSettingsDto getGddsAppSettings() {
      return this.appSettingsService.getGddsAppSettings();
   }

   @PostMapping
   public GddsAppSettingsDto updateGddsAppSettings(@RequestBody GddsAppSettingsDto appSettingsDto) {
      return this.appSettingsService.updateGddsAppSettings(appSettingsDto);
   }

   @GetMapping({"/log/access"})
   public AccessLogSettingsDto getAccessLogSettings() {
      return this.appSettingsService.getAccessLogSetting();
   }

   @PostMapping({"/log/access"})
   public AccessLogSettingsDto updateAccessLogSettings(@RequestBody AccessLogSettingsDto accessLogSettings) {
      return this.appSettingsService.updateAccessLogSettings(accessLogSettings);
   }

   @GetMapping({"/token/{dssAccountId}"})
   public TokenSettingsDto getTokenSettings(@PathVariable("dssAccountId") String dssAccountId) {
      return this.appSettingsService.getTokenSetting(dssAccountId);
   }

   @PostMapping({"/token"})
   public TokenSettingsDto updateTokenSettings(@RequestBody TokenSettingsDto tokenSettings) {
      return this.appSettingsService.updateTokenSettings(tokenSettings);
   }
}
