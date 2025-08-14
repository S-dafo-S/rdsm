package com.radiant.court;

import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.dto.GddsCourtDto;
import com.radiant.court.service.GddsCourtService;
import com.radiant.region.domain.dto.GddsRegionCourtPair;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/internal/v1/court"})
@Api(
   tags = {"Court management operations"}
)
public class GddsCourtController {
   @Autowired
   private GddsCourtService gddsCourtService;

   @ResponseStatus(HttpStatus.NO_CONTENT)
   @PostMapping({"/tree"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER', 'GDDS_SYSADMIN')")
   public void uploadCourtTree(@RequestParam("file") MultipartFile file) {
      this.gddsCourtService.uploadTree(file);
   }

   @GetMapping({"/{courtId}"})
   public GddsCourtDto get(@PathVariable("courtId") Long courtId) {
      return this.gddsCourtService.getById(courtId);
   }

   @GetMapping({"/minimal"})
   public List<CourtDto> getCourtMinimalList() {
      return this.gddsCourtService.getCourtListMinimalInfo();
   }

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public List<GddsRegionCourtPair> getCourtList(@RequestParam(value = "maxLevel",required = false) Long maxLevel, @RequestParam(value = "regions",required = false) Long[] regions) {
      return this.gddsCourtService.getCourtList(maxLevel, regions);
   }

   @PostMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public GddsCourtDto create(@RequestBody @Valid GddsCourtDto request) {
      return this.gddsCourtService.create(request);
   }

   @PutMapping({"/{courtId}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public GddsCourtDto update(@PathVariable("courtId") Long courtId, @RequestBody @Valid GddsCourtDto request) {
      return this.gddsCourtService.update(courtId, request);
   }

   @DeleteMapping({"/{courtId}"})
   @ResponseStatus(HttpStatus.NO_CONTENT)
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public void delete(@PathVariable("courtId") Long courtId) {
      this.gddsCourtService.delete(courtId);
   }

   @PostMapping({"/createTopWithRegion"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public GddsCourtDto createTopWithRegion(@RequestBody @Valid GddsCourtDto request) {
      return this.gddsCourtService.createTopWithRegion(request);
   }

   @GetMapping({"/region/{regionId}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public List<CourtDto> getCourtsByRegion(@PathVariable("regionId") Long regionId) {
      return this.gddsCourtService.getCourtsByRegion(regionId);
   }
}
