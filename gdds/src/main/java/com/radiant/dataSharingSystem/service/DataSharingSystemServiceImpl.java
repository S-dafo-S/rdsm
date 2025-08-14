package com.radiant.dataSharingSystem.service;

import com.radiant.account.domain.User;
import com.radiant.auth.service.CurrentUser;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.build.domain.Upgrade;
import com.radiant.build.domain.UpgradeStatus;
import com.radiant.build.domain.dto.UpgradeDto;
import com.radiant.build.service.BuildService;
import com.radiant.build.service.NetworkDssUpdateRequestDto;
import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.connect.GddsDssConnectResponse;
import com.radiant.connect.GddsDssUpdateRequest;
import com.radiant.connect.GddsDssUpdateResponse;
import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.GddsHostedCourt;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.service.GddsCourtHostService;
import com.radiant.court.service.GddsCourtService;
import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
import com.radiant.dataSharingSystem.domain.dto.DssDetailsDto;
import com.radiant.exception.dataSharingSystem.DSSVerificationException;
import com.radiant.exception.dataSharingSystem.DataSharingSystemNotEditable;
import com.radiant.exception.dataSharingSystem.DataSharingSystemNotFound;
import com.radiant.exception.dataSharingSystem.DuplicateDataSharingSystemAccountId;
import com.radiant.exception.dataSharingSystem.DuplicateDataSharingSystemName;
import com.radiant.exception.query.DssNotFoundForCourtException;
import com.radiant.kafka.DssUpdateStatusEvent;
import com.radiant.kafka.NetworkDssUpdateEvent;
import com.radiant.kafka.service.KafkaService;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.query.domain.GddsQueryRepository;
import com.radiant.query.domain.QueryBase;
import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.service.RegionService;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import com.radiant.util.DBUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
public class DataSharingSystemServiceImpl implements DataSharingSystemService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(DataSharingSystemServiceImpl.class);
   public static final int ACCESS_CHECK = 7;
   @Autowired
   private DnodeRepository dnodeRepository;
   @Autowired
   private BuildService buildService;
   @Autowired
   private GddsCourtService gddsCourtService;
   @Autowired
   private RegionService regionService;
   @Autowired
   private GddsCourtHostService gddsCourtHostService;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private GddsQueryRepository gddsQueryRepository;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;
   @Autowired
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Value("${kafka.topic.network-update}")
   private String networkUpdateTopic;

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addShortPeriodActivity(this, "Update DNodes connect status and access counts", (Object)null);
      }

   }

   public List<DssDetailsDto> getAll(Boolean addUpgradeInfo) {
      return (List)this.getAllDss().stream().map((dss) -> {
         DssDetailsDto dto = new DssDetailsDto(dss);
         if (addUpgradeInfo) {
            dto.setUpgradeInfo(this.buildService.getDssUpgradeInfo(dss.getAccountId()));
         }

         return dto;
      }).collect(Collectors.toList());
   }

   public List<DNode> getAllDss() {
      return this.dnodeRepository.findAll();
   }

   public DssDetailsDto get(Long id) {
      return new DssDetailsDto(this.getById(id));
   }

   public DNode getDss(Long id) {
      return this.getById(id);
   }

   public DssDetailsDto create(DssDetailsDto dssRequest) {
      DNode dnode = this.updateFromRequest(new DNode(), dssRequest);
      this.save(dnode);
      this.auditLogService.created((User)this.currentUser.get(), dnode).logMessage("Data node {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
      return new DssDetailsDto(dnode);
   }

   public DssDetailsDto update(Long id, DssDetailsDto dssRequest) {
      DNode dnode = this.updateFromRequest(this.getById(id), dssRequest);
      this.save(dnode);
      this.auditLogService.updated((User)this.currentUser.get(), dnode).logMessage("Data node {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new DssDetailsDto(dnode);
   }

   public void updateSecurity(Long id, DssDetailsDto request) {
      DNode dnode = this.getById(id);
      if (StringUtils.isNoneEmpty(new CharSequence[]{request.getToken()})) {
         dnode.setToken(request.getToken());
      }

      if (StringUtils.isNoneEmpty(new CharSequence[]{request.getQnodeToken()})) {
         dnode.setQnodeToken(request.getQnodeToken());
      }

      this.save(dnode);
   }

   public void delete(Long id) {
      DNode dnode = this.getById(id);
      this.checkIfEditable(dnode);
      AuditObject logObject = dnode.toAuditObject();
      this.dnodeAccessLogService.delete(dnode);
      this.dnodeRepository.delete(dnode);
      this.auditLogService.deleted((User)this.currentUser.get(), logObject).logMessage("Data node {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});
   }

   public GddsDssConnectResponse connect(GddsDssConnectRequest connectRequest) {
      Optional<DNode> optionalDss = this.dnodeRepository.findByAccountId(connectRequest.getAccountId());
      if (optionalDss.isPresent() && connectRequest.getAccountPassword().equals(((DNode)optionalDss.get()).getAccountPassword())) {
         DNode dnode = (DNode)optionalDss.get();
         if (connectRequest.getDeploymentCourtName() != null && !dnode.getDeployCourt().getName().equals(connectRequest.getDeploymentCourtName())) {
            throw new RuntimeException("Deployment court change is not allowed");
         } else {
            this.verifyDssUrl(connectRequest.getDssUrl(), connectRequest.getApplicationId());
            dnode.setDnodeUrl(connectRequest.getDssUrl());
            dnode.setToken(connectRequest.getDssToken());
            dnode.setVersion(connectRequest.getVersion());
            if (dnode.getQnodeToken() == null) {
               dnode.setQnodeToken(this.jwtTokenService.generateToken());
            }

            this.save(dnode);
            List<CourtDto> courtList = this.gddsCourtService.getCourtListMinimalInfo();
            List<RegionDto> regionList = new ArrayList(this.regionService.getList());
            List<Long> queryIds = (List)this.gddsQueryRepository.findAll().stream().map(QueryBase::getId).collect(Collectors.toList());
            return new GddsDssConnectResponse(dnode.getDeployCourt().getName(), courtList, regionList, queryIds, dnode.getQnodeToken(), dnode.getId());
         }
      } else {
         throw new BadCredentialsException("Unknown DNode credentials for DNode URL: " + connectRequest.getDssUrl() + " and DNode ID: " + connectRequest.getAccountId());
      }
   }

   public GddsDssUpdateResponse updateFromDss(GddsDssUpdateRequest updateRequest) {
      DNode dnode = (DNode)this.dnodeRepository.findByAccountId(updateRequest.getAccountId()).filter((req) -> updateRequest.getAccountPassword().equals(req.getAccountPassword())).orElseThrow(() -> new BadCredentialsException("Unknown DNode credentials for DSS URL: " + updateRequest.getDssUrl() + " and DNode ID: " + updateRequest.getAccountId()));
      dnode.setVersion(updateRequest.getVersion());
      return new GddsDssUpdateResponse(String.format("Version of DNode with account %s successfully updated to %s", updateRequest.getAccountId(), dnode.getVersion()));
   }

   public List<DNode> getByCourt(GddsCourt court) {
      return (List)this.gddsCourtHostService.getByCourt(court).stream().map(GddsHostedCourt::getDss).collect(Collectors.toList());
   }

   public List<DNode> getByCourtAndValidate(GddsCourt court) {
      List<DNode> dNodes = this.getByCourt(court);
      if (dNodes.isEmpty()) {
         throw new DssNotFoundForCourtException(court.getId());
      } else {
         return dNodes;
      }
   }

   public Boolean healthCheck(Long id) {
      DNode dnode = this.getById(id);

      try {
         this.verifyDssUrl(dnode.getDnodeUrl(), (String)null);
         return true;
      } catch (DSSVerificationException var4) {
         return false;
      }
   }

   public void performActivity(Object context) {
      LOG.trace("Update DNodes connect status and access counts");
      this.getAllDss().forEach((dss) -> {
         if (StringUtils.isNotEmpty(dss.getDnodeUrl())) {
            try {
               this.verifyDssUrl(dss.getDnodeUrl(), (String)null);
               dss.setConnectStatus(true);
            } catch (DSSVerificationException var4) {
               dss.setConnectStatus(false);
            }
         } else {
            dss.setConnectStatus(false);
         }

         Long allLogs = this.dnodeAccessLogService.countLastLogs(dss, DateUtils.addDays(new Date(), -7));
         Long failedLogs = this.dnodeAccessLogService.countLastFailedLogs(dss, DateUtils.addDays(new Date(), -7));
         dss.setAccessAll(allLogs);
         dss.setAccessFail(failedLogs);
         this.save(dss);
      });
   }

   public UpgradeDto initiateNetworkDssUpdate(NetworkDssUpdateRequestDto networkDssUpdateRequest) {
      Upgrade upgrade = this.buildService.initNetworkDssUpdate(networkDssUpdateRequest);
      NetworkDssUpdateEvent networkDssUpdateEvent = new NetworkDssUpdateEvent(upgrade.getAction(), upgrade.getUuid(), upgrade.getVersion(), upgrade.getDnodeAccountId());
      if (this.kafkaService != null) {
         this.kafkaService.sendMessage(this.networkUpdateTopic, networkDssUpdateEvent);
      }

      return new UpgradeDto(upgrade);
   }

   public String cliRequest(CliRequest request) {
      DNode dnode = this.dnodeRepository.getByName(request.getDssName());
      if (dnode == null) {
         throw new DataSharingSystemNotFound(request.getDssName());
      } else {
         String url = dnode.getDnodeUrl() + "/api/v1/query/cli";
         HttpHeaders headers = new HttpHeaders();
         if (dnode.getToken() != null) {
            headers.setBearerAuth(dnode.getToken());
         }

         HttpEntity<CliRequest> entity = new HttpEntity(request, headers);
         ResponseEntity<String> response = this.restTemplate.postForEntity(url, entity, String.class, new Object[0]);
         return (String)response.getBody();
      }
   }

   public void processDssUpdateStatusEvent(DssUpdateStatusEvent event) {
      this.buildService.deleteDssUpgradeAction(event.getDssAccountId());
      Upgrade upgrade = DssUpdateStatusEvent.toUpgradeEntity(event);
      upgrade.setUuid(event.getUuid());
      this.buildService.saveUpgradeAction(upgrade);
      if (upgrade.getStatus().equals(UpgradeStatus.RUNNING)) {
         this.buildService.setNetworkDssUpgradeStatus(upgrade, UpgradeStatus.RUNNING);
      }

      if (upgrade.getStatus().equals(UpgradeStatus.SUCCEEDED) && this.buildService.checkAllIndividualDssUpgradeFinished(upgrade)) {
         this.buildService.setNetworkDssUpgradeStatus(upgrade, UpgradeStatus.SUCCEEDED);
      }

   }

   private DNode getById(Long id) {
      return (DNode)this.dnodeRepository.findById(id).orElseThrow(() -> new DataSharingSystemNotFound(id));
   }

   private void checkIfEditable(DNode dnode) {
      if (StringUtils.isNoneEmpty(new CharSequence[]{dnode.getDnodeUrl()})) {
         throw new DataSharingSystemNotEditable(dnode.getId());
      }
   }

   private DNode save(DNode dnode) {
      try {
         return (DNode)this.dnodeRepository.save(dnode);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "dnode_name_uniq")) {
            throw new DuplicateDataSharingSystemName(dnode.getName());
         } else if (DBUtils.isConstraintViolated(exc, "dnode_account_id_uniq")) {
            throw new DuplicateDataSharingSystemAccountId(dnode.getAccountId());
         } else {
            throw exc;
         }
      }
   }

   private DNode updateFromRequest(DNode dnode, DssDetailsDto request) {
      GddsCourt deployCourt = this.gddsCourtService.getCourt(1L);
      dnode.setDeployCourt(deployCourt);
      dnode.setName(request.getName());
      dnode.setAccountId(request.getAccountId());
      if (StringUtils.isNoneEmpty(new CharSequence[]{request.getAccountPassword()})) {
         dnode.setAccountPassword(request.getAccountPassword());
      }

      return dnode;
   }

   private void verifyDssUrl(String dssUrl, String id) {
      String url = dssUrl + "/api/internal/v1/application/id";
      UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
      URI uri = builder.build().toUri();

      try {
         String result = (String)this.restTemplate.getForObject(uri, String.class);
         if (StringUtils.isEmpty(result) || id != null && !result.contains(id)) {
            throw new DSSVerificationException(dssUrl, "Wrong verification reply!");
         }
      } catch (ResourceAccessException var7) {
         throw new DSSVerificationException(dssUrl, "Connection error!");
      }
   }
}
