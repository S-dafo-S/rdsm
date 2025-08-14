package com.radiant.dataSharingSystem;

import com.radiant.build.domain.dto.UpgradeDto;
import com.radiant.build.service.NetworkDssUpdateRequestDto;
import com.radiant.dataSharingSystem.domain.dto.DssDetailsDto;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import io.swagger.annotations.Api;
import java.util.List;
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

@RestController
@RequestMapping({"/api/internal/v1/dnode"})
@Api(
   tags = {"DNode management operations"}
)
public class DataSharingSystemController {
   @Autowired
   private DataSharingSystemService dataSharingSystemService;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER', 'GDDS_SYSADMIN', 'JP_APP')")
   public List<DssDetailsDto> getDssList(@RequestParam(required = false,defaultValue = "false") Boolean addUpgradeInfo) {
      return this.dataSharingSystemService.getAll(addUpgradeInfo);
   }

   @GetMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public DssDetailsDto getDss(@PathVariable("id") Long id) {
      return this.dataSharingSystemService.get(id);
   }

   @PostMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public DssDetailsDto createDss(@RequestBody DssDetailsDto dssRequest) {
      return this.dataSharingSystemService.create(dssRequest);
   }

   @PutMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public DssDetailsDto updateDss(@PathVariable("id") Long id, @RequestBody DssDetailsDto dssRequest) {
      return this.dataSharingSystemService.update(id, dssRequest);
   }

   @DeleteMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void deleteDss(@PathVariable("id") Long id) {
      this.dataSharingSystemService.delete(id);
   }

   @PostMapping({"/network-update"})
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   @ResponseStatus(HttpStatus.CREATED)
   public UpgradeDto initiateNetworkDssUpdate(@RequestBody NetworkDssUpdateRequestDto updateRequest) {
      return this.dataSharingSystemService.initiateNetworkDssUpdate(updateRequest);
   }
}
