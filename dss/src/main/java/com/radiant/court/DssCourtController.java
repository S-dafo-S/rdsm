package com.radiant.court;

import com.radiant.CaseType;
import com.radiant.court.domain.dto.DssCourtDto;
import com.radiant.court.domain.dto.DssHostedCourtBatchRequest;
import com.radiant.court.domain.dto.DssHostedCourtDto;
import com.radiant.court.domain.dto.DssRegionCourtPair;
import com.radiant.court.service.DssCourtService;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.domain.dto.ExternalDataConnectorDto;
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

@RestController
@RequestMapping({"/api/internal/v1/court", "/api/v1/court"})
@Api(
   tags = {"Court management operations"}
)
public class DssCourtController {
   @Autowired
   private DssCourtService dssCourtService;

   @PostMapping({"/{courtId}/host"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public DssHostedCourtDto createHostedCourt(@PathVariable("courtId") Long courtId, @RequestBody @Valid DssHostedCourtDto hostRequest) {
      return this.dssCourtService.createHostAndNotify(courtId, hostRequest);
   }

   @PostMapping({"/host/batch"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void createMultipleHostedCourt(@RequestBody @Valid DssHostedCourtBatchRequest hostRequest) {
      this.dssCourtService.createHostInBatch(hostRequest);
   }

   @GetMapping({"/host"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<DssHostedCourtDto> getHostedCourts() {
      return this.dssCourtService.getHostedCourts();
   }

   @GetMapping({"/host/partial"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<DssRegionCourtPair> getHostedCourtsPartial(@RequestParam(value = "regions",required = false) Long[] regions) {
      return this.dssCourtService.getHostedCourtsPartial(regions);
   }

   @GetMapping
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<DssCourtDto> getCourts() {
      return this.dssCourtService.getAllCourts();
   }

   @GetMapping({"/{courtId}/host"})
   public DssHostedCourtDto getHostedCourt(@PathVariable("courtId") Long courtId) {
      return this.dssCourtService.getHostedCourt(courtId);
   }

   @PutMapping({"/{courtId}/host"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public DssHostedCourtDto updateHostedCourt(@PathVariable("courtId") Long courtId, @RequestBody @Valid DssHostedCourtDto hostRequest) {
      return this.dssCourtService.updateHostedCourt(courtId, hostRequest);
   }

   @ResponseStatus(HttpStatus.NO_CONTENT)
   @DeleteMapping({"/{courtId}/host"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public void deleteHostedCourt(@PathVariable("courtId") Long courtId) {
      this.dssCourtService.deleteHostedCourt(courtId);
   }

   @GetMapping({"/{courtId}/data-connector"})
   public List<ExternalDataConnectorDto> getConnectedDataConnector(@PathVariable("courtId") Long courtId, @RequestParam(required = false) CaseType caseType, @RequestParam(required = false) DataConnectorKind kind) {
      return this.dssCourtService.getCourtDataConnectors(courtId, caseType, kind);
   }
}
