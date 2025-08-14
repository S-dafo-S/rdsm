package com.radiant.region.service;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.court.domain.DssCourt;
import com.radiant.court.domain.repository.DssCourtRepository;
import com.radiant.exception.court.NoSuchCourtException;
import com.radiant.exception.court.NoSuchRegionException;
import com.radiant.exception.court.UnknownDeploymentCourtException;
import com.radiant.region.domain.DssRegion;
import com.radiant.region.domain.DssRegionRepository;
import com.radiant.region.domain.dto.RegionDto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DssRegionServiceImpl implements DssRegionService {
   @Autowired
   private DssRegionRepository dssRegionRepository;
   @Autowired
   private DssCourtRepository dssCourtRepository;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;

   public DssRegion getRootRegion() {
      String deploymentCourtName = this.applicationPropertyService.getStringValue("deployment_court_name");
      if (deploymentCourtName == null) {
         throw new UnknownDeploymentCourtException();
      } else {
         DssCourt deploymentCourt = (DssCourt)this.dssCourtRepository.findByName(deploymentCourtName).orElseThrow(() -> new NoSuchCourtException(deploymentCourtName));
         return deploymentCourt.getRegion();
      }
   }

   public List<RegionDto> getDssRegionsDtoSubtree() {
      return (List)this.getDssRegionsSubtree().stream().map((r) -> new RegionDto(r, r.getParent() != null ? r.getParent().getId() : null)).collect(Collectors.toList());
   }

   public DssRegion create(RegionDto region) {
      DssRegion newRegion = new DssRegion();
      Long parentId = region.getParent();
      if (parentId != null) {
         newRegion.setParent((DssRegion)this.dssRegionRepository.findById(parentId).orElseThrow(() -> new NoSuchRegionException(parentId)));
      }

      newRegion.setId(region.getId());
      newRegion.setLevel(region.getLevel());
      newRegion.setName(region.getName());
      newRegion.setShortName(region.getShortName());
      return (DssRegion)this.dssRegionRepository.saveAndFlush(newRegion);
   }

   public List<DssRegion> getDssRegionsSubtree() {
      DssRegion rootRegion = this.getRootRegion();
      List<DssRegion> rawRegions = this.dssRegionRepository.findByLevelGreaterThanEqual(rootRegion.getLevel());
      List<DssRegion> result = new ArrayList();
      this.collectSubregions(rootRegion, rawRegions, result);
      return result;
   }

   private void collectSubregions(DssRegion root, List<DssRegion> sourceRegions, List<DssRegion> collector) {
      collector.add(root);

      for(DssRegion ch : (List)sourceRegions.stream().filter((region) -> region.getParent() != null && root.getId().equals(region.getParent().getId())).collect(Collectors.toList())) {
         this.collectSubregions(ch, sourceRegions, collector);
      }

   }
}
