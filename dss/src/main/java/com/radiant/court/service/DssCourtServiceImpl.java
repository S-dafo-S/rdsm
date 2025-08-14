package com.radiant.court.service;

import com.google.common.collect.ImmutableList;
import com.radiant.CaseType;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.court.DataStore;
import com.radiant.court.domain.CourtDataStore;
import com.radiant.court.domain.DssCourt;
import com.radiant.court.domain.DssHostedCourt;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.dto.DssCourtDto;
import com.radiant.court.domain.dto.DssHostedCourtBatchRequest;
import com.radiant.court.domain.dto.DssHostedCourtDto;
import com.radiant.court.domain.dto.DssRegionCourtPair;
import com.radiant.court.domain.repository.CourtDataStoreRepository;
import com.radiant.court.domain.repository.DssCourtRepository;
import com.radiant.court.domain.repository.DssHostedCourtRepository;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.domain.dto.ExternalDataConnectorDto;
import com.radiant.dataConnector.service.DataConnectorServiceImpl;
import com.radiant.dto.NameId;
import com.radiant.exception.court.DuplicateCourtLocalId;
import com.radiant.exception.court.DuplicateCourtName;
import com.radiant.exception.court.FetchCourtException;
import com.radiant.exception.court.FetchCourtListException;
import com.radiant.exception.court.HostedCourtBatchCreationException;
import com.radiant.exception.court.HostedCourtException;
import com.radiant.exception.court.NoSuchCourtException;
import com.radiant.exception.court.NotHostedCourtException;
import com.radiant.kafka.DssCourtHostEventType;
import com.radiant.kafka.GddsCourtEvent;
import com.radiant.region.domain.DssRegion;
import com.radiant.region.domain.DssRegionRepository;
import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.service.DssRegionService;
import com.radiant.util.DBUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@ParametersAreNonnullByDefault
public class DssCourtServiceImpl implements DssCourtService {
   private static final Logger LOG = LoggerFactory.getLogger(DssCourtServiceImpl.class);
   @Autowired
   private DssCourtRepository dssCourtRepository;
   @Autowired
   private DssRegionRepository dssRegionRepository;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private DssHostedCourtRepository dssHostedCourtRepository;
   @Autowired
   private CourtDataStoreRepository courtDataStoreRepository;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private DssRegionService dssRegionService;
   @Autowired
   private ApplicationEventPublisher applicationEventPublisher;

   @Transactional(
      readOnly = true
   )
   public DssCourt getCourt(Long id) {
      return (DssCourt)this.dssCourtRepository.findById(id).orElseThrow(() -> new NoSuchCourtException(id));
   }

   private DssHostedCourtDto createHost(Long courtId, DssHostedCourtDto courtHostDto) {
      DssCourt court = this.getCourt(courtId);
      DssHostedCourt hostedCourt = new DssHostedCourt(courtHostDto.getLocalId(), courtHostDto.getLocalName(), court);
      DssHostedCourtDto hostedCourtDto = new DssHostedCourtDto(this.save(this.updateByRequest(hostedCourt, courtHostDto)));
      return hostedCourtDto;
   }

   public DssHostedCourtDto createHostAndNotify(Long courtId, DssHostedCourtDto courtHostDto) {
      DssHostedCourtDto dssHostedCourtDto = this.createHost(courtId, courtHostDto);
      this.applicationEventPublisher.publishEvent(new SendKafkaMessageEvent(DssCourtHostEventType.CREATED, Collections.singletonList(new CourtDto(dssHostedCourtDto.getCourt().getId(), dssHostedCourtDto.getCourt().getName()))));
      return dssHostedCourtDto;
   }

   public void createHostInBatch(DssHostedCourtBatchRequest batchRequest) {
      List<DssHostedCourt> toSave = new ArrayList();

      for(DssHostedCourtDto hostedCourtRequest : batchRequest.getHostedCourts()) {
         DssCourt court = this.getCourt(hostedCourtRequest.getCourt().getId());
         DssHostedCourt hostedCourt = new DssHostedCourt(hostedCourtRequest.getLocalId(), hostedCourtRequest.getLocalName(), court);
         this.updateBaseHostedCourtFieldsByRequest(hostedCourt, hostedCourtRequest);
         toSave.add(hostedCourt);
      }

      this.saveAllHostedCourts(toSave);
      this.applicationEventPublisher.publishEvent(new SendKafkaMessageEvent(DssCourtHostEventType.CREATED, (List)toSave.stream().map((hostedCourtx) -> new CourtDto(hostedCourtx.getCourt().getId(), hostedCourtx.getCourt().getName())).collect(Collectors.toList())));
   }

   @Transactional(
      readOnly = true
   )
   public Long countAll() {
      return this.dssCourtRepository.count();
   }

   public void saveAll(List<DssCourt> courts) {
      this.dssCourtRepository.saveAll(courts);
   }

   @Transactional(
      readOnly = true
   )
   public List<DssCourtDto> getAllCourts() {
      return (List)this.dssCourtRepository.findAll().stream().map(DssCourtDto::new).collect(Collectors.toList());
   }

   @Transactional(
      readOnly = true
   )
   public List<DssHostedCourtDto> getHostedCourts() {
      return (List)this.dssHostedCourtRepository.findAll().stream().map(DssHostedCourtDto::new).collect(Collectors.toList());
   }

   private DssCourt emptyDssCourt(Long level, DssRegion region) {
      return new DssCourt(-1L, "unknown", level, region);
   }

   private DssHostedCourt emptyDssHostedCourt(DssCourt court) {
      return new DssHostedCourt(-1L, "unknown", court);
   }

   private List<DssRegionCourtPair> addSubregionHostedCourts(@Nullable DssRegion region, @Nullable List<Long> regionList) {
      List<DssRegionCourtPair> res = new ArrayList();
      if (region != null) {
         List<DssRegionCourtPair> subRes = new ArrayList();
         List<DssRegion> childRegions = this.dssRegionRepository.findByParentOrderById(region);
         if (region.getLevel() < 3L) {
            childRegions.forEach((subRegion) -> subRes.addAll(this.addSubregionHostedCourts(subRegion, regionList)));
         } else {
            this.dssHostedCourtRepository.findAllByCourtRegionIdAndCourtLevel(region.getId(), 4L).stream().sorted(Comparator.comparing((hc) -> hc.getCourt().getId())).forEach((hc) -> subRes.add(new DssRegionCourtPair(region, hc, false)));
         }

         DssHostedCourt foundHostedCourt = this.dssHostedCourtRepository.getByRegionAndLevel(region.getId(), region.getLevel());
         DssCourt foundCourt = foundHostedCourt != null ? foundHostedCourt.getCourt() : this.emptyDssCourt(region.getLevel(), region);
         DssHostedCourt hostedCourtToAdd = foundHostedCourt != null ? foundHostedCourt : this.emptyDssHostedCourt(foundCourt);
         res.add(new DssRegionCourtPair(region, hostedCourtToAdd, true));
         if (regionList != null && regionList.contains(region.getId())) {
            res.addAll(subRes);
         }
      }

      return res;
   }

   public List<DssRegionCourtPair> getHostedCourtsPartial(@Nullable Long[] regions) {
      List<Long> regionList = regions != null ? Arrays.asList(regions) : null;
      DssRegion rootRegion = this.dssRegionService.getRootRegion();
      return this.addSubregionHostedCourts(rootRegion, regionList);
   }

   public DssHostedCourtDto getHostedCourt(Long courtId) {
      DssHostedCourt hostedCourt = this.getHostedCourtByCourt(courtId);
      return new DssHostedCourtDto(hostedCourt);
   }

   public DssHostedCourtDto updateHostedCourt(Long courtId, DssHostedCourtDto request) {
      DssHostedCourt hostedCourt = this.getHostedCourtByCourt(courtId);
      DssHostedCourtDto hostedCourtDto = new DssHostedCourtDto(this.save(this.updateByRequest(hostedCourt, request)));
      this.applicationEventPublisher.publishEvent(new SendKafkaMessageEvent(DssCourtHostEventType.UPDATED, Collections.singletonList(new CourtDto(hostedCourtDto.getCourt().getId(), hostedCourtDto.getCourt().getName()))));
      return hostedCourtDto;
   }

   @Transactional(
      readOnly = true
   )
   public @NotNull List<ExternalDataConnectorDto> getCourtDataConnectors(@NotNull Long courtId, @Nullable CaseType caseType, @Nullable DataConnectorKind kind) {
      DssCourt court = this.getCourt(courtId);
      if (court.getHostedCourt() == null) {
         throw new NotHostedCourtException(courtId);
      } else {
         return (List)court.getHostedCourt().getDataStores().stream().filter((store) -> store.getDataConnector() != null).filter((store) -> caseType == null || match(store.getDataStore(), caseType)).filter((store) -> kind == null || match(store.getDataStore(), kind)).map((store) -> new ExternalDataConnectorDto(store.getDataConnector())).collect(Collectors.toList());
      }
   }

   public void deleteHostedCourt(Long courtId) {
      DssHostedCourt hostedCourt = this.getHostedCourtByCourt(courtId);
      this.dssHostedCourtRepository.delete(hostedCourt);
      this.dssHostedCourtRepository.flush();
      this.applicationEventPublisher.publishEvent(new SendKafkaMessageEvent(DssCourtHostEventType.DELETED, Collections.singletonList(new CourtDto(hostedCourt.getCourt().getId(), hostedCourt.getCourt().getName()))));
   }

   @Transactional(
      readOnly = true
   )
   public DssHostedCourt getHostedCourtByCourt(Long courtId) {
      DssCourt originalCourt = this.getCourt(courtId);
      return (DssHostedCourt)this.dssHostedCourtRepository.findByCourt(originalCourt).orElseThrow(() -> new NotHostedCourtException(courtId));
   }

   public void deleteCourtStoresByDataConnector(DataConnector dataConnector) {
      this.courtDataStoreRepository.deleteByDataConnector(dataConnector);
   }

   public List<CourtDataStore> linkCourtsWithDataConnector(List<DssHostedCourt> hostedCourts, DataConnector dataConnector) {
      List<DataStore> dataStoreType = new ArrayList();
      if (dataConnector.getArchive()) {
         dataStoreType.add(DataConnectorServiceImpl.JDBC_TYPES.contains(dataConnector.getType()) ? DataStore.ARCHIVE_DB : DataStore.ARCHIVE_FS);
      }

      if (dataConnector.getLive()) {
         dataStoreType.add(DataConnectorServiceImpl.JDBC_TYPES.contains(dataConnector.getType()) ? DataStore.LIVE_DB : DataStore.LIVE_FS);
      }

      List<CourtDataStore> toSave = new ArrayList();

      for(DataStore dataStore : dataStoreType) {
         for(DssHostedCourt hostedCourt : hostedCourts) {
            CourtDataStore ds = new CourtDataStore(hostedCourt, dataStore, true);
            ds.setDataConnector(dataConnector);
            toSave.add(ds);
         }
      }

      return this.courtDataStoreRepository.saveAllAndFlush(toSave);
   }

   public boolean initializeDeployCourt(DssCourt court) {
      Optional<DssHostedCourt> hostedCourt = this.dssHostedCourtRepository.findByCourt(court);
      if (!hostedCourt.isPresent()) {
         LOG.info("Initializing deployment court");
         DssHostedCourtDto request = new DssHostedCourtDto();
         request.setCourt(new DssCourtDto(court.getId(), court.getName()));
         this.createHost(court.getId(), request);
         return true;
      } else {
         return false;
      }
   }

   public void processCourtUpdateEvent(GddsCourtEvent gddsCourtEvent) {
      LOG.info("Received in court topic: " + gddsCourtEvent);
      NameId data = gddsCourtEvent.getData();
      switch (gddsCourtEvent.getType()) {
         case LIST_UPLOADED:
            this.reloadCourtsList();
            break;
         case CREATED:
            this.create(data);
            break;
         case UPDATED:
            this.update(data);
            break;
         case DELETED:
            this.deleteCourt(data.getId());
            break;
         default:
            throw new IllegalStateException("Unknown event type or version");
      }

   }

   public DssCourt getByName(String name) {
      return (DssCourt)this.dssCourtRepository.findByName(name).orElseThrow(() -> new NoSuchCourtException(name));
   }

   private static boolean match(DataStore dataStore, CaseType caseType) {
      switch (caseType) {
         case LIVE:
            return ImmutableList.of(DataStore.LIVE_DB, DataStore.LIVE_FS).contains(dataStore);
         case ARCHIVED:
            return ImmutableList.of(DataStore.ARCHIVE_DB, DataStore.ARCHIVE_FS).contains(dataStore);
         default:
            throw new IllegalStateException("Unexpected value: " + caseType);
      }
   }

   private static boolean match(DataStore dataStore, DataConnectorKind kind) {
      switch (kind) {
         case DOCUMENT:
            return ImmutableList.of(DataStore.LIVE_FS, DataStore.ARCHIVE_FS).contains(dataStore);
         case DB:
            return ImmutableList.of(DataStore.LIVE_DB, DataStore.ARCHIVE_DB).contains(dataStore);
         default:
            throw new IllegalStateException("Unexpected value: " + kind);
      }
   }

   private DssHostedCourt updateByRequest(DssHostedCourt hostedCourt, DssHostedCourtDto request) {
      this.updateBaseHostedCourtFieldsByRequest(hostedCourt, request);
      return hostedCourt;
   }

   private DssHostedCourt updateBaseHostedCourtFieldsByRequest(DssHostedCourt hostedCourt, DssHostedCourtDto request) {
      hostedCourt.setDescription(request.getDescription());
      hostedCourt.setLocalId(request.getLocalId());
      hostedCourt.setLocalName(request.getLocalName());
      return hostedCourt;
   }

   private DssHostedCourt save(DssHostedCourt hostedCourt) {
      try {
         return (DssHostedCourt)this.dssHostedCourtRepository.saveAndFlush(hostedCourt);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "court_host_court_uniq")) {
            throw new HostedCourtException(hostedCourt.getCourt().getId());
         } else if (DBUtils.isConstraintViolated(exc, "court_host_local_id_uniq")) {
            throw new DuplicateCourtLocalId(hostedCourt.getLocalId());
         } else {
            throw exc;
         }
      }
   }

   private List<DssHostedCourt> saveAllHostedCourts(List<DssHostedCourt> hostedCourts) {
      try {
         return this.dssHostedCourtRepository.saveAllAndFlush(hostedCourts);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "court_host_court_uniq")) {
            throw new HostedCourtBatchCreationException(HostedCourtBatchCreationException.CourtBatchCreationMessageCode.SOME_OF_COURTS_ALREADY_HOSTED);
         } else if (DBUtils.isConstraintViolated(exc, "court_host_local_id_uniq")) {
            throw new HostedCourtBatchCreationException(HostedCourtBatchCreationException.CourtBatchCreationMessageCode.SOME_OF_COURT_LOCAL_ID_IS_DUPLICATE);
         } else {
            throw exc;
         }
      }
   }

   private void reloadCourtsList() {
      if (this.dssCourtRepository.count() != 0L) {
         LOG.info("Won't reload already existing courts list");
      } else {
         List<CourtDto> courts = this.fetchCourtsFromGdds();
         List<RegionDto> regions = this.fetchRegionsFromGdds();
         List<DssRegion> regionsToSave = new ArrayList();
         regions.forEach((dto) -> {
            DssRegion toSave = new DssRegion(dto.getId(), dto.getName(), dto.getShortName(), dto.getLevel());
            regionsToSave.add(toSave);
         });
         this.dssRegionRepository.saveAllAndFlush(regionsToSave);
         List<DssRegion> regionsToUpdate = this.dssRegionRepository.findAll();
         regions.forEach((dto) -> {
            DssRegion current = (DssRegion)regionsToUpdate.stream().filter((r) -> r.getId().equals(dto.getId())).findFirst().orElse((Object)null);
            DssRegion parent = (DssRegion)regionsToUpdate.stream().filter((r) -> r.getId().equals(dto.getParent())).findFirst().orElse((Object)null);
            if (current != null && parent != null) {
               current.setParent(parent);
            }

         });
         this.dssRegionRepository.saveAllAndFlush(regionsToUpdate);
         List<DssCourt> courtsToSave = new ArrayList();
         courts.forEach((dto) -> {
            DssRegion region = (DssRegion)regionsToUpdate.stream().filter((r) -> r.getId().equals(dto.getRegion().getId())).findFirst().orElse((Object)null);
            DssCourt toSave = new DssCourt(dto.getId(), dto.getName(), dto.getLevel(), region);
            courtsToSave.add(toSave);
         });
         this.saveAll(courtsToSave);
         LOG.info("Regions and Courts lists loaded");
      }
   }

   private @NotNull List<CourtDto> fetchCourtsFromGdds() {
      String url = this.gddsApiUrl("/api/internal/v1/court/minimal");
      HttpHeaders headers = this.buildHeaders();
      ResponseEntity<List<CourtDto>> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, headers == null ? null : new HttpEntity(headers), new CourtsListType(), new Object[0]);
      if (responseEntity.getBody() == null) {
         throw new FetchCourtListException();
      } else {
         return (List)responseEntity.getBody();
      }
   }

   private @NotNull List<RegionDto> fetchRegionsFromGdds() {
      String url = this.gddsApiUrl("/api/internal/v1/region");
      HttpHeaders headers = this.buildHeaders();
      ResponseEntity<List<RegionDto>> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, headers == null ? null : new HttpEntity(headers), new RegionsListType(), new Object[0]);
      if (responseEntity.getBody() == null) {
         throw new FetchCourtListException();
      } else {
         return (List)responseEntity.getBody();
      }
   }

   private void create(NameId data) {
      CourtDto gddsCourt = this.fetchCourtFromGdds(data.getId());
      DssRegion region = (DssRegion)this.dssRegionRepository.findById(gddsCourt.getRegion().getId()).orElse(this.dssRegionService.create(gddsCourt.getRegion()));
      DssCourt dssCourt = new DssCourt(gddsCourt.getId(), gddsCourt.getName(), gddsCourt.getLevel(), region);
      this.save(dssCourt);
   }

   private void update(NameId data) {
      CourtDto gddsCourt = this.fetchCourtFromGdds(data.getId());
      DssCourt dssCourt = this.getCourt(data.getId());
      dssCourt.setName(gddsCourt.getName());
      dssCourt.setLevel(gddsCourt.getLevel());
      this.save(dssCourt);
   }

   private void deleteCourt(Long id) {
      DssCourt court = this.getCourt(id);
      this.dssCourtRepository.delete(court);
   }

   private @NotNull CourtDto fetchCourtFromGdds(Long courtId) {
      String url = this.gddsApiUrl("/api/internal/v1/court/" + courtId);
      HttpHeaders headers = this.buildHeaders();
      ResponseEntity<CourtDto> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, headers == null ? null : new HttpEntity(headers), CourtDto.class, new Object[0]);
      if (responseEntity.getBody() == null) {
         throw new FetchCourtException(courtId);
      } else {
         return (CourtDto)responseEntity.getBody();
      }
   }

   private @NotNull String gddsApiUrl(String methodPath) {
      String gddsUrl = this.applicationPropertyService.getStringValue("gdds_url");
      if (gddsUrl == null) {
         throw new RuntimeException("Unknown GDDS URL");
      } else {
         return gddsUrl + methodPath;
      }
   }

   private HttpHeaders buildHeaders() {
      String gddsToken = this.applicationPropertyService.getStringValue("gdds_token");
      if (gddsToken != null) {
         HttpHeaders headers = new HttpHeaders();
         headers.setBearerAuth(gddsToken);
         return headers;
      } else {
         return null;
      }
   }

   private void save(DssCourt court) {
      try {
         this.dssCourtRepository.saveAndFlush(court);
      } catch (DataIntegrityViolationException ex) {
         if (DBUtils.isConstraintViolated(ex, "court_name_uniq")) {
            throw new DuplicateCourtName(court.getName());
         } else {
            throw ex;
         }
      }
   }

   private static class CourtsListType extends ParameterizedTypeReference<List<CourtDto>> {
      private CourtsListType() {
      }
   }

   private static class RegionsListType extends ParameterizedTypeReference<List<RegionDto>> {
      private RegionsListType() {
      }
   }
}
