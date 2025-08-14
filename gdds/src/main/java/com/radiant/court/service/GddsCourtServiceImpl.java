package com.radiant.court.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.applicationProperty.domain.ApplicationProperty;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.GddsCourtRepository;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.court.domain.dto.GddsCourtDto;
import com.radiant.court.domain.dto.GddsCourtListPlain;
import com.radiant.court.domain.dto.GddsCourtPlainDto;
import com.radiant.court.domain.dto.GddsHostedCourtListPlain;
import com.radiant.court.domain.dto.GddsHostedCourtPlainDto;
import com.radiant.court.domain.dto.VersionedCourtTree;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.court.CourtVersionException;
import com.radiant.exception.court.DeployedCourtDeletionException;
import com.radiant.exception.court.DuplicateCourtId;
import com.radiant.exception.court.DuplicateCourtName;
import com.radiant.exception.court.HostCourtDeletionException;
import com.radiant.exception.court.InvalidCourtLevelException;
import com.radiant.exception.court.InvalidCourtLevelInRegion;
import com.radiant.exception.court.MultipleTopCourtException;
import com.radiant.exception.court.NoSuchCourtException;
import com.radiant.exception.court.NoSuchRegionException;
import com.radiant.exception.court.RegionTopCourtWithChildrenDeletionException;
import com.radiant.exception.region.DuplicateRegionNameException;
import com.radiant.exception.region.InvalidRegionLevelException;
import com.radiant.exception.region.MissingParentRegionException;
import com.radiant.exception.region.MultipleRootRegionException;
import com.radiant.exception.region.RootRegionNotFoundException;
import com.radiant.kafka.GddsCourtEvent;
import com.radiant.kafka.GddsCourtEventType;
import com.radiant.kafka.service.KafkaService;
import com.radiant.region.domain.GddsRegion;
import com.radiant.region.domain.GddsRegionRepository;
import com.radiant.region.domain.RegionBase;
import com.radiant.region.domain.RegionCourt;
import com.radiant.region.domain.RegionCourtRepository;
import com.radiant.region.domain.dto.GddsRegionCourtPair;
import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.service.RegionService;
import com.radiant.util.DBUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class GddsCourtServiceImpl implements GddsCourtService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsCourtServiceImpl.class);
   private static final long MAX_COURT_LEVEL = 4L;
   private static final String COURT_VERSION_PATTERN = "yyyy/MM/dd HH:mm:ss";
   private static final String COURT_LIST_VERSION = "court_list_version";
   @Value("${kafka.dss.topic.court}")
   private String dssCourtTopic;
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Autowired
   private GddsCourtRepository gddsCourtRepository;
   @Autowired
   private RegionCourtRepository regionCourtRepository;
   @Autowired
   private GddsRegionRepository gddsRegionRepository;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private RegionService regionService;
   @Autowired
   private GddsCourtHostService gddsCourtHostService;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private DnodeRepository dnodeRepository;

   public List<GddsRegionCourtPair> getCourtList(@Nullable Long levelLimit, @Nullable Long[] regions) {
      List<Long> regionList = regions != null ? Arrays.asList(regions) : null;
      List<GddsRegionCourtPair> rawResult = this.gddsRegionRepository.findRegionsWithTopCourts(levelLimit, regionList);
      List<GddsRegionCourtPair> result = new ArrayList();
      if (rawResult.isEmpty()) {
         return result;
      } else {
         List<GddsRegionCourtPair> roots = regions == null ? (List)rawResult.stream().filter((pair) -> pair.getLevel() == 1L && pair.getCourt().getLevel() == 1L).collect(Collectors.toList()) : (List)rawResult.stream().filter((pair) -> pair.getId().equals(regions[0]) && pair.getCourt().getLevel().equals(pair.getLevel())).collect(Collectors.toList());
         if (roots.isEmpty()) {
            throw new RootRegionNotFoundException();
         } else if (roots.size() > 1) {
            throw new MultipleRootRegionException();
         } else {
            GddsRegionCourtPair root = (GddsRegionCourtPair)roots.get(0);
            this.addRegionCourtChildren(root, rawResult, result);
            return result;
         }
      }
   }

   public List<CourtDto> getCourtListMinimalInfo() {
      return (List)this.gddsCourtRepository.findAll().stream().map((court) -> {
         GddsRegion region = court.getParentRegion().getRegion();
         RegionDto dto = new RegionDto(region, region.getParent() != null ? region.getParent().getId() : null);
         return new CourtDto(court.getId(), court.getName(), court.getLevel(), dto);
      }).collect(Collectors.toList());
   }

   public GddsCourtListPlain getCourtListPlain() {
      List<GddsCourtPlainDto> courtList = (List)this.gddsCourtRepository.findAll().stream().map(GddsCourtPlainDto::new).collect(Collectors.toList());
      GddsCourtListPlain result = new GddsCourtListPlain();
      result.setCourtList(courtList);
      return result;
   }

   public CourtTreeNode getCourtTree(boolean hostedOnly) {
      List<GddsRegion> regions = this.gddsRegionRepository.findAll();
      GddsRegion root = (GddsRegion)regions.stream().filter((reg) -> reg.getLevel().equals(1L)).findAny().orElseThrow(() -> new RuntimeException("Root not found"));
      if (hostedOnly) {
         return this.gddsCourtHostService.getCourtTree(root);
      } else {
         Map<Long, List<RegionCourt>> courtsByRegion = this.getRegionCourtMap();
         LOG.trace("Construct court tree starting...");
         CourtTreeNode result = this.getCourtTree(regions, root, courtsByRegion);
         LOG.trace("Construct court tree finished");
         return result;
      }
   }

   public CourtTreeNode getCourtTree(Long regionId) {
      List<GddsRegion> regions = this.gddsRegionRepository.findAll();
      GddsRegion root = (GddsRegion)this.gddsRegionRepository.findById(regionId).orElseThrow(() -> new NoSuchRegionException(regionId));
      Map<Long, List<RegionCourt>> courtsByRegion = this.getRegionCourtMap();
      return this.getCourtTree(regions, root, courtsByRegion);
   }

   public void uploadTree(MultipartFile file) {
      CourtTreeNode courtTree;
      try {
         courtTree = (CourtTreeNode)this.objectMapper.readValue(file.getBytes(), CourtTreeNode.class);
      } catch (IOException e) {
         throw new RdsmIOException(e);
      }

      this.handleUploadedTree(courtTree);
      this.updateCourtVersion();
   }

   public GddsCourtDto getById(Long courtId) {
      return new GddsCourtDto(this.getCourt(courtId));
   }

   public List<CourtDto> getCourtsByRegion(Long regionId) {
      GddsRegion gddsRegion = (GddsRegion)this.gddsRegionRepository.findById(regionId).orElseThrow(() -> new NoSuchRegionException(regionId));
      return (List)this.regionCourtRepository.findByRegion(gddsRegion).stream().map((pair) -> new CourtDto(pair.getCourt(), gddsRegion, gddsRegion.getParent() != null ? gddsRegion.getParent().getId() : null)).collect(Collectors.toList());
   }

   public void createInitialStructure() {
      if (!this.gddsCourtRepository.existsById(1L)) {
         LOG.info("Creating initial GDDS Data");
         GddsCourt court = new GddsCourt();
         court.setId(1L);
         court.setName("Fake Court");
         court.setShortName("Fake Court");
         court.setLevel(1L);
         this.gddsCourtRepository.saveAndFlush(court);
         GddsRegion region = (GddsRegion)this.gddsRegionRepository.findById(1L).orElseGet(() -> {
            GddsRegion newRegion = new GddsRegion();
            newRegion.setId(1L);
            newRegion.setName("Region");
            newRegion.setShortName("Region");
            newRegion.setLevel(1L);
            return newRegion;
         });
         region.addCourtToChildren(court);
         this.gddsRegionRepository.saveAndFlush(region);
      }

   }

   public GddsCourtDto create(GddsCourtDto request) {
      if (this.gddsCourtRepository.findById(request.getId()).isPresent()) {
         throw new DuplicateCourtId(request.getId());
      } else {
         GddsCourt court = new GddsCourt();
         court.setId(request.getId());
         court.setName(request.getName());
         court.setShortName(request.getShortName());
         court.setLevel(request.getLevel());
         GddsRegion region = (GddsRegion)this.gddsRegionRepository.findById(request.getRegion().getId()).orElseThrow(() -> new NoSuchRegionException(request.getRegion().getId()));
         this.validateCourtLevel(court.getLevel(), region.getLevel(), court.getName());
         if (court.getLevel().equals(region.getLevel()) && this.gddsCourtRepository.existsByParentRegionRegionAndLevel(region, court.getLevel())) {
            throw new MultipleTopCourtException(court.getName(), region.getName());
         } else if (court.getLevel() > region.getLevel() && region.getLevel() != 3L) {
            throw new InvalidCourtLevelInRegion(court.getName(), court.getLevel(), region.getLevel(), InvalidCourtLevelInRegion.InvalidCourtLevelMessageCode.GREATER_COURT_LEVEL_FOR_NOT_MAX_LEVEL_REGION);
         } else {
            if (court.getLevel() == 4L) {
               List<GddsCourt> possibleParents = this.gddsCourtRepository.findByParentRegionRegionAndLevel(region, court.getLevel() - 1L);
               if (possibleParents.isEmpty()) {
                  throw new RuntimeException("No court of upper level is found in region");
               }
            }

            this.updateFromRequest(court, request);
            region.addCourtToChildren(court);
            this.saveRegionAndCourt(region, request);
            GddsCourtDto savedCourt = this.getById(request.getId());
            this.updateCourtVersion();
            this.sendKafkaMessage(GddsCourtEventType.CREATED, savedCourt);
            return savedCourt;
         }
      }
   }

   public GddsCourtDto update(Long courtId, GddsCourtDto request) {
      GddsCourt court = this.getCourt(courtId);
      court.setName(request.getName());
      court.setShortName(request.getShortName());
      GddsCourtDto dto = new GddsCourtDto(this.save(court, request));
      this.updateCourtVersion();
      this.sendKafkaMessage(GddsCourtEventType.UPDATED, dto);
      return dto;
   }

   public void delete(Long courtId) {
      try {
         Optional<GddsCourt> court = this.gddsCourtRepository.findById(courtId);
         court.ifPresent((gddsCourt) -> {
            RegionCourt rc = ((GddsCourt)court.get()).getParentRegion();
            GddsRegion region = rc.getRegion();
            if (region.getChildrenCourts().stream().anyMatch((childrenCourt) -> childrenCourt.getCourt().getLevel() > ((GddsCourt)court.get()).getLevel())) {
               throw new RegionTopCourtWithChildrenDeletionException(courtId);
            } else {
               region.getChildrenCourts().remove(rc);
               this.gddsRegionRepository.saveAndFlush(region);
               this.gddsCourtRepository.deleteById(courtId);
               this.gddsCourtRepository.flush();
            }
         });
         this.updateCourtVersion();
         this.sendKafkaMessage(GddsCourtEventType.DELETED, new CourtDto(courtId, "deleted"));
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "dnode_deploy_court_fk")) {
            throw new DeployedCourtDeletionException(courtId);
         } else if (DBUtils.isConstraintViolated(e, "court_host_court_fk")) {
            throw new HostCourtDeletionException(courtId);
         } else {
            throw e;
         }
      }
   }

   @Transactional(
      readOnly = true
   )
   public GddsCourt getCourt(Long courtId) {
      return (GddsCourt)this.gddsCourtRepository.findById(courtId).orElseThrow(() -> new NoSuchCourtException(courtId));
   }

   public GddsCourtDto createTopWithRegion(GddsCourtDto request) {
      if (this.gddsCourtRepository.findById(request.getId()).isPresent()) {
         throw new DuplicateCourtId(request.getId());
      } else {
         GddsRegion region = this.regionService.construct(request.getRegion());
         GddsCourt court = new GddsCourt();
         court.setId(request.getId());
         court.setName(request.getName());
         court.setShortName(request.getShortName());
         court.setLevel(region.getLevel());
         this.updateFromRequest(court, request);
         region.addCourtToChildren(court);
         this.saveRegionAndCourt(region, request);
         GddsCourtDto savedCourt = this.getById(request.getId());
         this.sendKafkaMessage(GddsCourtEventType.CREATED, savedCourt);
         return savedCourt;
      }
   }

   public VersionedCourtTree getGreaterCourtTreeVersion(@Nullable String versionGreater, boolean hostedOnly) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      String versionString = this.applicationPropertyService.getStringValue("court_list_version");
      Date jpVersion = null;

      Date courtListVersion;
      try {
         courtListVersion = dateFormat.parse(versionString);
         if (versionGreater != null) {
            jpVersion = dateFormat.parse(versionGreater);
         }
        } catch (ParseException e) {
           throw new RuntimeException("Failed to parse court list version");
        }

      if (jpVersion != null && jpVersion.after(courtListVersion)) {
         throw new CourtVersionException(versionGreater);
      } else if (jpVersion != null && jpVersion.compareTo(courtListVersion) == 0) {
         return new VersionedCourtTree(versionString);
      } else {
         VersionedCourtTree result = new VersionedCourtTree(versionString);
         result.setCourts(this.getCourtTree(hostedOnly));
         return result;
      }
   }

   public CourtDto getCourtByName(String name) {
      GddsCourt court = (GddsCourt)this.gddsCourtRepository.findByName(name).orElseThrow(() -> new NoSuchCourtException(name));
      return this.mapToCourtDto(court);
   }

   public List<CourtDto> getCourtByPartialName(String name) {
      return name == null ? Collections.emptyList() : (List)this.gddsCourtRepository.findByPartialName(name).stream().map(this::mapToCourtDto).collect(Collectors.toList());
   }

   public GddsHostedCourtListPlain getHostedCourtListPlain() {
      List<GddsHostedCourtPlainDto> hostedCourtList = (List)this.gddsCourtHostService.getAll().stream().map(GddsHostedCourtPlainDto::new).collect(Collectors.toList());
      GddsHostedCourtListPlain result = new GddsHostedCourtListPlain();
      result.setHostedCourtList(hostedCourtList);
      return result;
   }

   private GddsCourt save(GddsCourt court, GddsCourtDto request) {
      try {
         return (GddsCourt)this.gddsCourtRepository.saveAndFlush(this.updateFromRequest(court, request));
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "court_name_uniq")) {
            throw new DuplicateCourtName(request.getName());
         } else {
            throw e;
         }
      }
   }

   private GddsRegion saveRegionAndCourt(GddsRegion region, GddsCourtDto courtRequest) {
      try {
         return (GddsRegion)this.gddsRegionRepository.saveAndFlush(region);
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "region_name_uniq")) {
            throw new DuplicateRegionNameException(region.getName());
         } else if (DBUtils.isConstraintViolated(e, "court_name_uniq")) {
            throw new DuplicateCourtName(courtRequest.getName());
         } else {
            throw e;
         }
      }
   }

   private void handleUploadedTree(CourtTreeNode courtTree) {
      LOG.info("Court tree handle start...");
      if (this.gddsCourtRepository.count() == 0L) {
         List<GddsRegion> regionsToSave = new ArrayList();
         List<GddsCourt> toSave = new ArrayList();
         this.handleTreeNode(courtTree, (GddsRegion)null, regionsToSave, toSave, false);
         this.gddsCourtRepository.saveAllAndFlush(toSave);
         this.gddsRegionRepository.saveAllAndFlush(regionsToSave);
         LOG.info("Court tree handle finished");
         this.sendKafkaMessage(GddsCourtEventType.LIST_UPLOADED, (CourtDto)null);
      } else if (this.gddsRegionRepository.existsByOutdatedIsTrue()) {
         List<GddsRegion> renewRegions = new ArrayList();
         this.handleTreeNode(courtTree, (GddsRegion)null, renewRegions, new ArrayList(), true);
         this.switchFromOutdatedRegions(renewRegions);
         LOG.info("Court tree handle finished");
      } else {
         throw new RuntimeException("Bad request: courts already exist");
      }
   }

   private void handleTreeNode(CourtTreeNode treeNode, @Nullable GddsRegion parentRegion, List<GddsRegion> regionCollector, List<GddsCourt> courtCollector, boolean skipCourts) {
      LOG.trace("Handle container {}", treeNode.getName());
      int i = 0;
      if (treeNode.getLevel() > 3L) {
         throw new InvalidRegionLevelException(treeNode.getName(), treeNode.getLevel());
      } else if (parentRegion == null && treeNode.getLevel() > 1L) {
         throw new MissingParentRegionException(treeNode.getName());
      } else if (treeNode.getLevel() == 1L && regionCollector.stream().anyMatch((regionx) -> regionx.getLevel() == 1L)) {
         throw new MultipleRootRegionException();
      } else {
         GddsRegion region = new GddsRegion(treeNode.getId(), treeNode.getName(), treeNode.getShortName(), parentRegion, treeNode.getLevel());
         regionCollector.add(region);

         for(CourtTreeNode children : treeNode.getChildren()) {
            if (i == 0) {
               ++i;
               if (!skipCourts) {
                  GddsCourt topCourt = this.constructCourt(children, region);
                  region.addCourtToChildren(topCourt);
                  courtCollector.add(topCourt);
               }
            } else if (children.getIsContainer()) {
               this.handleTreeNode(children, region, regionCollector, courtCollector, skipCourts);
            } else if (!skipCourts) {
               GddsCourt leaveCourt = this.constructCourt(children, region);
               courtCollector.add(leaveCourt);
               region.addCourtToChildren(leaveCourt);
            }
         }

      }
   }

   private GddsCourt constructCourt(CourtTreeNode node, GddsRegion region) {
      this.validateCourtLevel(node.getLevel(), region.getLevel(), node.getName());
      if (node.getLevel().equals(region.getLevel()) && region.getChildrenCourts().stream().anyMatch((c) -> c.getCourt().getLevel().equals(region.getLevel()))) {
         throw new MultipleTopCourtException(node.getName(), region.getName());
      } else if (node.getLevel() > region.getLevel() && region.getLevel() != 3L) {
         throw new InvalidCourtLevelInRegion(node.getName(), node.getLevel(), region.getLevel(), InvalidCourtLevelInRegion.InvalidCourtLevelMessageCode.GREATER_COURT_LEVEL_FOR_NOT_MAX_LEVEL_REGION);
      } else {
         GddsCourt court = new GddsCourt(node.getId(), node.getName(), node.getLevel());
         court.setShortName(node.getShortName());
         LOG.info("Creating court {} with region {}", court.getName(), region.getName());
         return court;
      }
   }

   private CourtTreeNode getCourtTree(List<GddsRegion> allRegions, GddsRegion region, Map<Long, List<RegionCourt>> courtsByRegion) {
      CourtTreeNode node = GddsCourtUtils.createLeafNode(region);
      if (courtsByRegion.containsKey(region.getId())) {
         Stream<CourtTreeNode> courtNodeStream = ((List<RegionCourt>)courtsByRegion.get(region.getId())).stream()
            .sorted(Comparator.comparing(RegionCourt::getOrder))
            .map(RegionCourt::getCourt)
            .map(GddsCourtUtils::createLeafNode);
         List<CourtTreeNode> children = node.getChildren();
         courtNodeStream.forEach(children::add);
      }

      List<GddsRegion> subRegions = (List)allRegions.stream().filter((c) -> c.getParent() != null && c.getParent().getId().equals(region.getId())).collect(Collectors.toList());
      subRegions.forEach((children) -> node.getChildren().add(this.getCourtTree(allRegions, children, courtsByRegion)));
      return node;
   }

   private GddsCourt updateFromRequest(GddsCourt court, GddsCourtDto request) {
      court.setContactName(request.getContactName());
      court.setContactPhone(request.getContactPhone());
      court.setContactEmail(request.getContactEmail());
      court.setDescription(request.getDescription());
      return court;
   }

   private void sendKafkaMessage(GddsCourtEventType type, @Nullable CourtDto data) {
      if (this.kafkaService != null) {
         this.kafkaService.sendMessage(this.dssCourtTopic, new GddsCourtEvent(type, data));
      } else {
         LOG.warn("Kafka isn't active");
      }

   }

   private void switchFromOutdatedRegions(List<GddsRegion> addedRegions) {
      List<GddsRegion> outdated = this.gddsRegionRepository.findByOutdatedIsTrue();
      Set<Long> oldIds = (Set)outdated.stream().map(RegionBase::getId).collect(Collectors.toSet());
      Set<Long> newIds = (Set)addedRegions.stream().map(RegionBase::getId).collect(Collectors.toSet());
      Set<Long> overlap = new HashSet(oldIds);
      overlap.retainAll(newIds);
      Set<Long> regionsToSwitch = new HashSet(oldIds);
      if (!overlap.isEmpty()) {
         Collection<GddsRegion> temp = this.resolveIdsConflicts(outdated, oldIds, newIds, overlap);
         regionsToSwitch.removeAll(overlap);
         regionsToSwitch.addAll((Collection)temp.stream().map(RegionBase::getId).collect(Collectors.toSet()));
      }

      this.gddsRegionRepository.saveAllAndFlush(addedRegions);
      List<GddsRegion> regionsToBeSwitchedFrom = this.gddsRegionRepository.findAllById(regionsToSwitch);
      Map<GddsRegion, GddsRegion> map = new HashMap();

      for(GddsRegion region : regionsToBeSwitchedFrom) {
         GddsRegion renewed = (GddsRegion)addedRegions.stream().filter((reg) -> reg.getName().equals(region.getName())).findAny().orElseThrow(() -> new RuntimeException("Failed to get region for outdated: " + region.getName()));

         for(RegionCourt regionCourt : region.getChildrenCourts()) {
            renewed.addCourtToChildren(regionCourt.getCourt());
         }

         map.put(region, renewed);
      }

      this.gddsRegionRepository.saveAllAndFlush(map.values());
      List<GddsRegion> conflictedParentRegions = this.gddsRegionRepository.findByParentIdIn((Collection)map.keySet().stream().map(RegionBase::getId).collect(Collectors.toSet()));

      for(GddsRegion conflictParentCourt : conflictedParentRegions) {
         conflictParentCourt.setParent((GddsRegion)map.get(conflictParentCourt.getParent()));
      }

      this.gddsRegionRepository.saveAllAndFlush(conflictedParentRegions);
      this.gddsRegionRepository.deleteAll(map.keySet());
      this.gddsRegionRepository.flush();
   }

   private Collection<GddsRegion> resolveIdsConflicts(Collection<GddsRegion> outdatedRegions, Set<Long> oldIds, Set<Long> newIds, Set<Long> overlap) {
      Map<GddsRegion, GddsRegion> oldRegionToTemp = new HashMap();
      long maxRegionId = Stream.concat(oldIds.stream(), newIds.stream()).mapToLong(Long::longValue).max().orElseThrow(() -> new RuntimeException("Ids must not be empty"));
      long idShift = 10000L;
      long unoccupiedId = maxRegionId + 10000L;

      for(GddsRegion region : outdatedRegions) {
         if (overlap.contains(region.getId())) {
            GddsRegion temp = new GddsRegion(unoccupiedId, region.getName(), region.getShortName(), region.getParent(), region.getLevel());
            ++unoccupiedId;
            oldRegionToTemp.put(region, temp);
         }
      }

      this.gddsRegionRepository.saveAllAndFlush(oldRegionToTemp.values());

      for(Map.Entry<GddsRegion, GddsRegion> entry : oldRegionToTemp.entrySet()) {
         for(RegionCourt cc : ((GddsRegion)entry.getKey()).getChildrenCourts()) {
            ((GddsRegion)entry.getValue()).addCourtToChildren(cc.getCourt());
         }
      }

      this.gddsRegionRepository.saveAllAndFlush(oldRegionToTemp.values());
      List<GddsRegion> conflictedParentRegions = this.gddsRegionRepository.findByParentIdIn(overlap);

      for(GddsRegion conflictParentReg : conflictedParentRegions) {
         conflictParentReg.setParent((GddsRegion)oldRegionToTemp.get(conflictParentReg.getParent()));
      }

      this.gddsRegionRepository.saveAllAndFlush(conflictedParentRegions);
      this.gddsRegionRepository.deleteAll(oldRegionToTemp.keySet());
      this.gddsRegionRepository.flush();
      Collection<GddsRegion> temp = this.gddsRegionRepository.findAllById((Iterable)oldRegionToTemp.values().stream().map(RegionBase::getId).collect(Collectors.toSet()));

      for(GddsRegion t : temp) {
         t.setOutdated(true);
      }

      return this.gddsRegionRepository.saveAllAndFlush(temp);
   }

   private void addRegionCourtChildren(GddsRegionCourtPair root, List<GddsRegionCourtPair> allPairs, List<GddsRegionCourtPair> collector) {
      this.setAccessCounts(root);
      collector.add(root);
      List<GddsRegionCourtPair> childrenCourts = (List)allPairs.stream().filter((pair) -> root.getId().equals(pair.getId()) && pair.isNotTopCourtPair()).peek(this::setAccessCounts).sorted(Comparator.comparing((pair) -> pair.getCourt().getId())).collect(Collectors.toList());
      collector.addAll(childrenCourts);

      for(GddsRegionCourtPair ch : (List)allPairs.stream().filter((pair) -> root.getId().equals(pair.getParent()) && pair.isTopCourtPair()).collect(Collectors.toList())) {
         this.addRegionCourtChildren(ch, allPairs, collector);
      }

   }

   private void validateCourtLevel(Long courtLevel, Long regionLevel, String courtName) {
      if (courtLevel > 4L) {
         throw new InvalidCourtLevelException(courtName, courtLevel);
      } else if (courtLevel < regionLevel) {
         throw new InvalidCourtLevelInRegion(courtName, courtLevel, regionLevel, InvalidCourtLevelInRegion.InvalidCourtLevelMessageCode.COURT_LEVEL_MUST_BE_GREAT_EQUAL_REGION_LEVEL);
      }
   }

   private void updateCourtVersion() {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      this.applicationPropertyService.updateValues(Collections.singletonList(new ApplicationProperty("court_list_version", dateFormat.format(new Date()), (String)null)));
   }

   private Map<Long, List<RegionCourt>> getRegionCourtMap() {
      Map<Long, List<RegionCourt>> courtsByRegion = new HashMap();

      for(RegionCourt court : this.regionCourtRepository.getAllRegionCourts()) {
         ((List)courtsByRegion.computeIfAbsent(court.getRegion().getId(), (id) -> new ArrayList())).add(court);
      }

      return courtsByRegion;
   }

   private CourtDto mapToCourtDto(GddsCourt court) {
      GddsRegion region = court.getParentRegion().getRegion();
      return new CourtDto(court, region, region.getParent() != null ? region.getParent().getId() : null);
   }

   private void setAccessCounts(GddsRegionCourtPair pair) {
      DNode foundDNode = (DNode)this.dnodeRepository.findAll().stream().filter((dss) -> dss.getDeployCourt().getId().equals(pair.getCourt().getId())).findFirst().orElse((Object)null);
      if (foundDNode != null) {
         pair.setAccessAll(foundDNode.getAccessAll());
         pair.setAccessFail(foundDNode.getAccessFail());
      }

   }
}
