package com.radiant.region.service;

import com.radiant.region.domain.GddsRegion;
import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.domain.dto.RegionStatusDto;
import com.radiant.region.domain.dto.RegionWithTopCourtDto;
import java.util.List;

public interface RegionService {
   List<RegionWithTopCourtDto> getList();

   RegionStatusDto getOutdatedStatus();

   GddsRegion construct(RegionDto request);
}
