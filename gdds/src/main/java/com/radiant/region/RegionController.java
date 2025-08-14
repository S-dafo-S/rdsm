package com.radiant.region;

import com.radiant.region.domain.dto.RegionStatusDto;
import com.radiant.region.domain.dto.RegionWithTopCourtDto;
import com.radiant.region.service.RegionService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/internal/v1/region"})
@Api(
   tags = {"Region operations"}
)
public class RegionController {
   @Autowired
   private RegionService service;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public List<RegionWithTopCourtDto> getList() {
      return this.service.getList();
   }

   @GetMapping({"/status"})
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   public RegionStatusDto checkRegionOutdatedStatus() {
      return this.service.getOutdatedStatus();
   }
}
