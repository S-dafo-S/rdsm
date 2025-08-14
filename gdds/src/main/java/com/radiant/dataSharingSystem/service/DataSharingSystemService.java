package com.radiant.dataSharingSystem.service;

import com.radiant.build.domain.dto.UpgradeDto;
import com.radiant.build.service.NetworkDssUpdateRequestDto;
import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.connect.GddsDssConnectResponse;
import com.radiant.connect.GddsDssUpdateRequest;
import com.radiant.connect.GddsDssUpdateResponse;
import com.radiant.court.domain.GddsCourt;
import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.dto.DssDetailsDto;
import com.radiant.kafka.DssUpdateStatusEvent;
import java.util.List;

public interface DataSharingSystemService {
   List<DssDetailsDto> getAll(Boolean addUpgradeInfo);

   List<DNode> getAllDss();

   DssDetailsDto get(Long id);

   DNode getDss(Long id);

   DssDetailsDto create(DssDetailsDto dssRequest);

   DssDetailsDto update(Long id, DssDetailsDto dssRequest);

   void updateSecurity(Long id, DssDetailsDto dssRequest);

   void delete(Long id);

   GddsDssConnectResponse connect(GddsDssConnectRequest connectRequest);

   GddsDssUpdateResponse updateFromDss(GddsDssUpdateRequest updateRequest);

   List<DNode> getByCourt(GddsCourt court);

   /** @deprecated */
   @Deprecated
   List<DNode> getByCourtAndValidate(GddsCourt court);

   Boolean healthCheck(Long id);

   UpgradeDto initiateNetworkDssUpdate(NetworkDssUpdateRequestDto version);

   String cliRequest(CliRequest request);

   void processDssUpdateStatusEvent(DssUpdateStatusEvent event);
}
