package com.radiant.appSettings.service;

import com.radiant.appSettings.domain.TokenSettingsDto;

public interface DssAppSettingsService extends AppSettingsService {
   TokenSettingsDto getTokenSetting();

   TokenSettingsDto updateTokenSettings(TokenSettingsDto request);
}
