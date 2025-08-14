package com.radiant.court.service;

import com.radiant.CaseType;
import com.radiant.court.domain.CourtDataStore;
import com.radiant.court.domain.DssCourt;
import com.radiant.court.domain.DssHostedCourt;
import com.radiant.court.domain.dto.DssCourtDto;
import com.radiant.court.domain.dto.DssHostedCourtBatchRequest;
import com.radiant.court.domain.dto.DssHostedCourtDto;
import com.radiant.court.domain.dto.DssRegionCourtPair;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.domain.dto.ExternalDataConnectorDto;
import com.radiant.kafka.GddsCourtEvent;
import java.util.List;
import javax.annotation.Nullable;

/** @deprecated */
@Deprecated
public interface DssCourtService {
   DssCourt getCourt(Long id);

   DssHostedCourtDto createHostAndNotify(Long courtId, DssHostedCourtDto courtHostDto);

   void createHostInBatch(DssHostedCourtBatchRequest batchRequest);

   Long countAll();

   void saveAll(List<DssCourt> courts);

   List<DssCourtDto> getAllCourts();

   List<DssHostedCourtDto> getHostedCourts();

   List<DssRegionCourtPair> getHostedCourtsPartial(@Nullable Long[] regions);

   DssHostedCourtDto getHostedCourt(Long courtId);

   DssHostedCourtDto updateHostedCourt(Long courtId, DssHostedCourtDto request);

   /** @deprecated */
   @Deprecated
   List<ExternalDataConnectorDto> getCourtDataConnectors(Long courtId, CaseType caseType, DataConnectorKind kind);

   void deleteHostedCourt(Long courtId);

   DssHostedCourt getHostedCourtByCourt(Long courtId);

   void deleteCourtStoresByDataConnector(DataConnector dataConnector);

   List<CourtDataStore> linkCourtsWithDataConnector(List<DssHostedCourt> hostedCourts, DataConnector dataConnector);

   void processCourtUpdateEvent(GddsCourtEvent gddsCourtEvent);

   boolean initializeDeployCourt(DssCourt court);

   DssCourt getByName(String name);
}
