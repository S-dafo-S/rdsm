package com.radiant.region.service;

import com.radiant.region.domain.DssRegion;
import com.radiant.region.domain.dto.RegionDto;
import java.util.List;

public interface DssRegionService {
   DssRegion getRootRegion();

   List<DssRegion> getDssRegionsSubtree();

   List<RegionDto> getDssRegionsDtoSubtree();

   DssRegion create(RegionDto region);
}
