package com.radiant.region;

import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.service.DssRegionService;
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
   tags = {"Region management operations"}
)
public class DssRegionController {
   @Autowired
   private DssRegionService dssRegionService;

   @GetMapping({"/subtree"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<RegionDto> getDssRegionsSubtree() {
      return this.dssRegionService.getDssRegionsDtoSubtree();
   }
}
