package com.radiant.appSettings.service;

import com.radiant.appSettings.domain.AccessLogSettingsDto;
import com.radiant.appSettings.domain.GddsAppSettingsDto;
import com.radiant.appSettings.domain.TokenSettingsDto;

public interface GddsAppSettingsService extends AppSettingsService {
   GddsAppSettingsDto getGddsAppSettings();

   GddsAppSettingsDto updateGddsAppSettings(GddsAppSettingsDto request);

   AccessLogSettingsDto getAccessLogSetting();

   AccessLogSettingsDto updateAccessLogSettings(AccessLogSettingsDto accessLogSettings);

   TokenSettingsDto getTokenSetting(String dssAccountId);

   TokenSettingsDto updateTokenSettings(TokenSettingsDto request);
}
