package com.radiant.gddsConnect.service;

import com.radiant.gddsConnect.domain.dto.GddsConnectDto;

public interface GddsConnectReportService {
   void reportVersion(GddsConnectDto connectInfo, String releaseVersion);
}
