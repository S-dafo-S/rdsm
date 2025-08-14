package com.radiant.gddsConnect.service;

import com.radiant.account.domain.User;
import com.radiant.applicationProperty.domain.ApplicationProperty;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.CurrentUser;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.build.ReleaseInfoService;
import com.radiant.build.service.BuildService;
import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.connect.GddsDssConnectResponse;
import com.radiant.court.domain.DssCourt;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.repository.DssCourtRepository;
import com.radiant.court.service.DssCourtService;
import com.radiant.court.service.SendKafkaMessageEvent;
import com.radiant.gddsConnect.domain.dto.GddsConnectDto;
import com.radiant.kafka.DssCourtHostEventType;
import com.radiant.kafka.service.KafkaService;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.query.service.DssQueryService;
import com.radiant.region.domain.DssRegion;
import com.radiant.region.domain.DssRegionRepository;
import com.radiant.util.ValidationUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class GddsConnectServiceImpl implements GddsConnectService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsConnectServiceImpl.class);
   public static final String GDDS_URL = "gdds_url";
   public static final String TOKEN = "token";
   public static final String GDDS_TOKEN = "gdds_token";
   public static final String DSS_ACCOUNT_ID = "dss_id";
   public static final String DEPLOYMENT_COURT_NAME = "deployment_court_name";
   public static final String DSS_ACCOUNT_PASSWORD = "dss_account_password";
   private static final String DDS_URL = "dds_url";
   private static final String GDDS_CONNECT_DATE = "gdds_connect_date";
   private static final String QNODE_DNODE_ID = "qnode_dnode_id";
   private static final String REPORTED_RELEASE_VERSION = "dss_reported_version";
   private static final long UPDATE_ATTEMPTS_INTERVAL_SEC = 60L;
   private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   @Autowired
   private ApplicationPropertyService propertyService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private DssCourtService courtService;
   @Autowired
   private DssRegionRepository dssRegionRepository;
   @Autowired
   private DssCourtRepository dssCourtRepository;
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private DssQueryService dssQueryService;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private ApplicationEventPublisher applicationEventPublisher;
   @Autowired
   private BuildService buildService;
   @Autowired
   private ReleaseInfoService releaseInfoService;
   @Autowired
   private TaskScheduler executor;
   @Autowired
   private GddsConnectReportService gddsConnectReportService;

   @PostConstruct
   protected void init() {
      this.scheduleVersionReport(60L);
   }

   private void scheduleVersionReport(long seconds) {
      this.executor.schedule(this::updateDssVersionOnGdds, LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant());
   }

   public void updateDssVersionOnGdds() {
      GddsConnectDto connectInfo = this.getConnectInfo();
      if (StringUtils.isBlank(connectInfo.getGddsUrl())) {
         LOG.info("DSS is not connected, nowhere to report");
      } else {
         String releaseVersion = this.releaseInfoService.getVersion();

         try {
            this.gddsConnectReportService.reportVersion(connectInfo, releaseVersion);
         } catch (Exception e) {
            LOG.error("Failed to report release version {} to GDDS {}, will retry in {} seconds", new Object[]{releaseVersion, this.getConnectInfo().getGddsUrl(), 60L, e});
            this.scheduleVersionReport(60L);
         }

      }
   }

   public GddsConnectDto getConnectInfo() {
      return this.getConnectInfo(false);
   }

   public GddsConnectDto getConnectInfo(Boolean needRestart) {
      GddsConnectDto result = new GddsConnectDto(needRestart);
      result.setGddsUrl(this.propertyService.getStringValue("gdds_url"));
      result.setAccountId(this.propertyService.getStringValue("dss_id"));
      result.setDssUrl(this.propertyService.getStringValue("dds_url"));
      result.setQnodeDnodeId(this.propertyService.getStringValue("qnode_dnode_id"));
      String deployCourtName = this.propertyService.getStringValue("deployment_court_name");
      result.setDeployCourtName(deployCourtName);
      if (StringUtils.isNoneEmpty(new CharSequence[]{deployCourtName})) {
         DssCourt deployCourt = this.courtService.getByName(deployCourtName);
         result.setDeployCourtId(deployCourt.getId());
         result.setDeployRegionId(deployCourt.getRegion().getId());
         result.setDeployRegionName(deployCourt.getRegion().getName());
      }

      String gddsConnectDate = this.propertyService.getStringValue("gdds_connect_date");
      if (gddsConnectDate != null) {
         try {
            result.setConnectDate(this.DATE_FORMAT.parse(gddsConnectDate));
         } catch (ParseException e) {
            throw new RuntimeException("Failed to parse gdds_connect_date application property");
         }
      }

      return result;
   }

   public GddsConnectDto executeConnect(GddsDssConnectRequest connectRequest) {
      ValidationUtils.isUrlValid(connectRequest.getGddsUrl());
      ValidationUtils.isUrlValid(connectRequest.getDssUrl());
      boolean gddsUrlChanged = this.propertyService.getStringValue("gdds_url") != null && !this.propertyService.getStringValue("gdds_url").equals(connectRequest.getGddsUrl());
      String url = connectRequest.getGddsUrl() + "/api/public/v1/dnode/connect";
      if (connectRequest.getAccountPassword() == null) {
         connectRequest.setAccountPassword(this.propertyService.getStringValue("dss_account_password"));
      }

      String token = this.propertyService.getStringValue("token");
      if (token == null) {
         token = this.jwtTokenService.generateToken();
         this.propertyService.updateValues(Collections.singletonList(new ApplicationProperty("token", token, (String)null)));
      }

      connectRequest.setDssToken(token);
      connectRequest.setApplicationId(this.propertyService.getAppInstanceID());
      connectRequest.setDeploymentCourtName(this.propertyService.getStringValue("deployment_court_name"));
      String releaseVersion = this.buildService.getReleaseInfo().getReleaseVersion();
      connectRequest.setVersion(releaseVersion);
      ResponseEntity<GddsDssConnectResponse> response = this.restTemplate.postForEntity(url, connectRequest, GddsDssConnectResponse.class, new Object[0]);
      GddsDssConnectResponse body = (GddsDssConnectResponse)response.getBody();
      if (response.getStatusCode().equals(HttpStatus.OK) && body != null) {
         String deployCourtName = body.getDeployCourtName();
         List<ApplicationProperty> updatedProps = new ArrayList();
         updatedProps.add(new ApplicationProperty("gdds_url", connectRequest.getGddsUrl(), (String)null));
         updatedProps.add(new ApplicationProperty("dss_id", connectRequest.getAccountId(), (String)null));
         updatedProps.add(new ApplicationProperty("qnode_dnode_id", Long.toString(body.getId()), (String)null));
         updatedProps.add(new ApplicationProperty("dss_account_password", (String)null, connectRequest.getAccountPassword()));
         updatedProps.add(new ApplicationProperty("dds_url", connectRequest.getDssUrl(), (String)null));
         updatedProps.add(new ApplicationProperty("deployment_court_name", deployCourtName, (String)null));
         updatedProps.add(new ApplicationProperty("token", token, (String)null));
         updatedProps.add(new ApplicationProperty("gdds_token", body.getGddsToken(), (String)null));
         updatedProps.add(new ApplicationProperty("gdds_connect_date", this.DATE_FORMAT.format(new Date()), (String)null));
         updatedProps.add(new ApplicationProperty("dss_reported_version", releaseVersion, (String)null));
         this.propertyService.updateValues(updatedProps);
         if (this.dssRegionRepository.count() == 0L) {
            List<DssRegion> regionsToAdd = new ArrayList();
            body.getRegionList().forEach((dto) -> regionsToAdd.add(new DssRegion(dto.getId(), dto.getName(), dto.getShortName(), dto.getLevel())));
            this.dssRegionRepository.saveAllAndFlush(regionsToAdd);
            User actor = (User)this.currentUser.get();
            List<DssRegion> regionsToUpdate = this.dssRegionRepository.findAll();
            body.getRegionList().forEach((dto) -> {
               if (dto.getParent() != null) {
                  DssRegion current = (DssRegion)regionsToUpdate.stream().filter((r) -> r.getId().equals(dto.getId())).findFirst().orElse((Object)null);
                  DssRegion parent = (DssRegion)regionsToUpdate.stream().filter((r) -> r.getId().equals(dto.getParent())).findFirst().orElse((Object)null);
                  if (current != null && parent != null) {
                     current.setParent(parent);
                  }
               }

            });
            this.dssRegionRepository.saveAll(regionsToUpdate);
         }

         if (this.courtService.countAll() == 0L || this.courtService.countAll() > 0L && this.dssCourtRepository.existsByRegionIsNull()) {
            List<DssCourt> courts = new ArrayList();
            List<DssRegion> regions = this.dssRegionRepository.findAll();
            body.getCourtList().forEach((courtDto) -> {
               DssRegion region = (DssRegion)regions.stream().filter((r) -> r.getId().equals(courtDto.getRegion().getId())).findFirst().orElse((Object)null);
               DssCourt toSave = new DssCourt(courtDto.getId(), courtDto.getName(), courtDto.getLevel(), region);
               courts.add(toSave);
            });
            this.courtService.saveAll(courts);
         }

         DssCourt dssCourt = this.courtService.getByName(deployCourtName);
         boolean gddsNotify = gddsUrlChanged || this.courtService.initializeDeployCourt(dssCourt);
         boolean needRestart = this.dssQueryService.syncQueries(body.getQueries());
         this.connectToKafka();
         if (gddsNotify) {
            this.applicationEventPublisher.publishEvent(new SendKafkaMessageEvent(DssCourtHostEventType.CREATED, Collections.singletonList(new CourtDto(dssCourt.getId(), dssCourt.getName()))));
         }

         return this.getConnectInfo(needRestart);
      } else {
         throw new RuntimeException("Failed to connect");
      }
   }

   public boolean isConnected() {
      GddsConnectDto connectInfo = this.getConnectInfo();
      return connectInfo.getGddsUrl() != null && connectInfo.getAccountId() != null;
   }

   public void connectToKafka() {
      try {
         if (this.kafkaService != null && this.isConnected()) {
            this.kafkaService.creatListeners();
         } else {
            LOG.warn("Kafka disabled or DSS isn't connected");
         }
      } catch (KafkaException e) {
         LOG.error("Can't setup kafka", e);
      }

   }
}
