package com.radiant.query.service;

import com.radiant.query.domain.dto.GddsQueryDetailsDto;
import com.radiant.query.domain.dto.GddsQueryDto;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public interface GddsQueryService {
   List<GddsQueryDto> getAllQueries();

   GddsQueryDetailsDto getQueryDetails(Long queryId);

   MultiValueMap<String, Object> getQueryProgramDetails(@NotNull Long queryId);

   GddsQueryDetailsDto updateQuery(Long queryId, GddsQueryDetailsDto updateRequest, MultipartFile jarFile, MultipartFile docFile, MultipartFile sampleCodeFile);

   GddsQueryDetailsDto patchQuery(Long queryId, GddsQueryDetailsDto updateRequest);

   GddsQueryDetailsDto createQuery(GddsQueryDetailsDto createRequest, MultipartFile jarFile, MultipartFile docFile, MultipartFile sampleCodeFile);

   ResponseEntity<InputStreamResource> downloadDoc(String queryName);

   ResponseEntity<InputStreamResource> downloadSampleCode(String queryName);
}
