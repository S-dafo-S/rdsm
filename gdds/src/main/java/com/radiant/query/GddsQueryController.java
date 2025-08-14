package com.radiant.query;

import com.radiant.query.domain.dto.GddsQueryDetailsDto;
import com.radiant.query.domain.dto.GddsQueryDto;
import com.radiant.query.service.GddsQueryService;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/internal/v1/query"})
@Api(
   tags = {"Public court management operations"}
)
public class GddsQueryController {
   @Autowired
   private GddsQueryService gddsQueryService;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER', 'GDDS_SYSADMIN')")
   public List<GddsQueryDto> getAll() {
      return this.gddsQueryService.getAllQueries();
   }

   @GetMapping({"/{queryId}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public GddsQueryDetailsDto getQuery(@PathVariable("queryId") Long queryId) {
      return this.gddsQueryService.getQueryDetails(queryId);
   }

   @GetMapping({"/{queryId}/program"})
   public ResponseEntity<MultiValueMap<String, Object>> getQueryWithProgram(@PathVariable("queryId") Long queryId) {
      MultiValueMap<String, Object> data = this.gddsQueryService.getQueryProgramDetails(queryId);
      return ResponseEntity.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(data);
   }

   @PutMapping({"/{queryId}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public GddsQueryDetailsDto updateQuery(@PathVariable("queryId") Long queryId, @RequestPart(value = "file",required = false) MultipartFile jarFile, @RequestPart(value = "docFile",required = false) MultipartFile docFile, @RequestPart(value = "sampleCodeFile",required = false) MultipartFile sampleCodeFile, @RequestPart @Valid GddsQueryDetailsDto updateRequest) {
      return this.gddsQueryService.updateQuery(queryId, updateRequest, jarFile, docFile, sampleCodeFile);
   }

   @PatchMapping({"/{queryId}"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public GddsQueryDetailsDto patchQuery(@PathVariable("queryId") Long queryId, @RequestBody @Valid GddsQueryDetailsDto updateRequest) {
      return this.gddsQueryService.patchQuery(queryId, updateRequest);
   }

   @PostMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public GddsQueryDetailsDto createQuery(@RequestPart(value = "file",required = false) MultipartFile jarFile, @RequestPart(value = "docFile",required = false) MultipartFile docFile, @RequestPart(value = "sampleCodeFile",required = false) MultipartFile sampleCodeFile, @RequestPart @Valid GddsQueryDetailsDto createRequest) {
      return this.gddsQueryService.createQuery(createRequest, jarFile, docFile, sampleCodeFile);
   }

   @GetMapping({"/{queryName}/doc"})
   public ResponseEntity<InputStreamResource> downloadQueryDoc(@PathVariable("queryName") String queryName) {
      return this.gddsQueryService.downloadDoc(queryName);
   }

   @GetMapping({"/{queryName}/sample"})
   public ResponseEntity<InputStreamResource> downloadQuerySample(@PathVariable("queryName") String queryName) {
      return this.gddsQueryService.downloadSampleCode(queryName);
   }
}
