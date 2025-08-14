package com.radiant.gddsConnect.service;

import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.gddsConnect.domain.dto.GddsConnectDto;

public interface GddsConnectService {
   GddsConnectDto getConnectInfo();

   GddsConnectDto getConnectInfo(Boolean needRestart);

   GddsConnectDto executeConnect(GddsDssConnectRequest connectRequest);

   boolean isConnected();

   void connectToKafka();
}
