package com.radiant.court.service;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.GddsCourtHostRepository;
import com.radiant.court.domain.GddsCourtRepository;
import com.radiant.court.domain.GddsHostedCourt;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.court.domain.dto.HostedCourDto;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
import com.radiant.dto.NameId;
import com.radiant.exception.court.CourtAlreadyHosted;
import com.radiant.exception.court.FetchCourtHostException;
import com.radiant.exception.court.NoSuchCourtException;
import com.radiant.exception.dataSharingSystem.DataSharingSystemNotFound;
import com.radiant.kafka.DssCourtHostEvent;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.region.domain.GddsRegion;
import com.radiant.region.domain.GddsRegionRepository;
import com.radiant.util.DBUtils;
import com.radiant.util.TransactionUtils;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class GddsCourtHostServiceImpl implements GddsCourtHostService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsCourtHostServiceImpl.class);
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private DnodeRepository dnodeRepository;
   @Autowired
   private GddsCourtRepository courtRepository;
   @Autowired
   private GddsCourtHostRepository gddsCourtHostRepository;
   @Autowired
   private GddsRegionRepository gddsRegionRepository;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;

   public List<GddsHostedCourt> getByCourt(GddsCourt court) {
      return this.gddsCourtHostRepository.findByCourt(court);
   }

   @Transactional
   public CourtTreeNode getCourtTree(GddsRegion region) {
      CourtTreeNode node = GddsCourtUtils.createLeafNode(region);

      for(GddsHostedCourt hostedCourt : this.gddsCourtHostRepository.findAllByCourt_ParentRegionRegion(region)) {
         node.getChildren().add(GddsCourtUtils.createLeafNode(hostedCourt.getCourt()));
      }

      for(GddsRegion subRegion : this.gddsRegionRepository.findAllByParent(region)) {
         CourtTreeNode subNode = this.getCourtTree(subRegion);
         if (subNode != null) {
            node.getChildren().add(subNode);
         }
      }

      return node.getChildren().size() > 0 ? node : null;
   }

   @Transactional
   public List<GddsHostedCourt> getAll() {
      return this.gddsCourtHostRepository.findAll();
   }

   public void processCourtHostUpdateEvent(DssCourtHostEvent event) {
      LOG.info("Received in court topic: " + event);
      NameId data = event.getData();
      DNode dnode = (DNode)this.dnodeRepository.findByAccountId(event.getDssId()).orElseThrow(() -> new DataSharingSystemNotFound(event.getDssId()));
      switch (event.getType()) {
         case CREATED:
            this.created(dnode, data);
            break;
         case UPDATED:
            this.updated(dnode, data);
            break;
         case DELETED:
            this.deleted(dnode, data);
            break;
         default:
            throw new IllegalStateException("Unknown event type or version");
      }

   }

   private void created(DNode dnode, NameId data) {
      Optional<GddsHostedCourt> optionalHost = this.gddsCourtHostRepository.findByDssAndCourtId(dnode, data.getId());
      GddsHostedCourt host;
      if (optionalHost.isPresent()) {
         TransactionUtils.runWithCheckForUndesirableActiveTransaction(() -> this.serviceLogManagementService.log(LogLevel.WARNING, "Hosted court " + data.getId() + " already exist", data.getId()), LOG, "serviceLogManagement log");
         host = (GddsHostedCourt)optionalHost.get();
      } else {
         host = this.create(dnode);
      }

      this.update(host, dnode, data);
   }

   private void updated(DNode dnode, NameId data) {
      GddsHostedCourt host = (GddsHostedCourt)this.gddsCourtHostRepository.findByDssAndCourtId(dnode, data.getId()).orElse(this.create(dnode));
      this.update(host, dnode, data);
   }

   private @NotNull GddsHostedCourt create(DNode dnode) {
      GddsHostedCourt host = new GddsHostedCourt();
      host.setDss(dnode);
      return host;
   }

   private void update(GddsHostedCourt host, DNode dnode, NameId data) {
      GddsCourt court = this.findCourt(data.getId());
      host.setCourt(court);
      HostedCourDto hostDataFromDss = this.fetchCourtHostFromDss(dnode, data.getId());
      host.setLocalId(hostDataFromDss.getLocalId());
      host.setLocalName(hostDataFromDss.getLocalName());
      host.setDescription(hostDataFromDss.getDescription());

      try {
         this.gddsCourtHostRepository.save(host);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "court_host_dss_uniq")) {
            throw new CourtAlreadyHosted(host.getCourt().getId());
         } else {
            throw exc;
         }
      }
   }

   private @NotNull GddsCourt findCourt(Long id) {
      return (GddsCourt)this.courtRepository.findById(id).orElseThrow(() -> new NoSuchCourtException(id));
   }

   private @NotNull HostedCourDto fetchCourtHostFromDss(DNode dnode, Long courtId) {
      String url = dnode.getDnodeUrl() + "/api/v1/court/" + courtId + "/host";
      HttpHeaders headers = new HttpHeaders();
      if (dnode.getToken() != null) {
         headers.setBearerAuth(dnode.getToken());
      }

      try {
         ResponseEntity<HostedCourDto> responseEntity = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers), HostedCourDto.class, new Object[0]), LOG, "fetchCourtHostFromDss");
         if (responseEntity.getBody() == null) {
            throw new FetchCourtHostException(dnode.getDnodeUrl());
         } else {
            this.dnodeAccessLogService.log(dnode, url, true, (String)null);
            return (HostedCourDto)responseEntity.getBody();
         }
      } catch (Exception ex) {
         this.dnodeAccessLogService.log(dnode, url, false, ex.getMessage());
         throw ex;
      }
   }

   private void deleted(DNode dnode, NameId data) {
      this.gddsCourtHostRepository.deleteByDssAndCourtId(dnode, data.getId());
   }
}
