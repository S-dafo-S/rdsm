package com.radiant.query;

import com.radiant.query.domain.dto.DssQueryDetailsDto;
import com.radiant.query.domain.dto.DssQueryDto;
import com.radiant.query.domain.dto.DssQueryImplDto;
import com.radiant.query.domain.dto.QueryTestExecutionDto;
import com.radiant.query.service.DssQueryService;
import com.radiant.query.service.QueryExecutionService;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/internal/v1/query"})
@Api(
   tags = {"Query management operations"}
)
public class DssQueryController {
   @Autowired
   private DssQueryService dssQueryService;
   @Autowired
   private QueryExecutionService queryExecutionService;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<DssQueryDto> getAll() {
      return this.dssQueryService.getAllQueries();
   }

   @GetMapping({"/{queryId}"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public DssQueryDetailsDto getQuery(@PathVariable("queryId") Long queryId) {
      return this.dssQueryService.getQueryDetails(queryId);
   }

   @GetMapping({"/{queryId}/impl"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public List<DssQueryImplDto> getQueryImpls(@PathVariable("queryId") Long queryId) {
      return this.dssQueryService.getImplementations(queryId);
   }

   @GetMapping({"/{queryId}/impl/{implId}"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public DssQueryImplDto getImpl(@PathVariable("queryId") Long queryId, @PathVariable("implId") Long implId) {
      return this.dssQueryService.getQueryImplementation(queryId, implId);
   }

   @PostMapping({"/{queryId}/impl"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public DssQueryImplDto createImpl(@PathVariable("queryId") Long queryId, @RequestPart("createRequest") @Valid DssQueryImplDto implRequest, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.dssQueryService.addImplementation(queryId, implRequest, jarFile);
   }

   @PutMapping({"/{queryId}/impl/{implId}"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public DssQueryImplDto updateImpl(@PathVariable("queryId") Long queryId, @PathVariable("implId") Long implId, @RequestPart("updateRequest") @Valid DssQueryImplDto updateRequest, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.dssQueryService.updateImplementation(queryId, implId, updateRequest, jarFile);
   }

   @PatchMapping({"/{queryId}/impl/{implId}"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public DssQueryImplDto updateImplStatus(@PathVariable("queryId") Long queryId, @PathVariable("implId") Long implId, @RequestBody @Valid DssQueryImplDto updateRequest) {
      return this.dssQueryService.updateImplStatus(queryId, implId, updateRequest);
   }

   @ResponseStatus(HttpStatus.NO_CONTENT)
   @DeleteMapping({"/{queryId}/impl/{implId}"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   public void deleteImpl(@PathVariable("queryId") Long queryId, @PathVariable("implId") Long implId) {
      this.dssQueryService.deleteImplementation(queryId, implId);
   }

   @PostMapping({"/{queryId}/impl/test"})
   @PreAuthorize("hasAnyAuthority('DSS_DATA_MANAGER')")
   @ResponseStatus(HttpStatus.CREATED)
   public Object testExecution(@PathVariable("queryId") Long queryId, @RequestBody @Valid QueryTestExecutionDto testExecutionDto) {
      return this.queryExecutionService.testExecution(queryId, testExecutionDto);
   }
}
