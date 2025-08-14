package com.radiant.query.service;

import com.radiant.kafka.GddsQueryEvent;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.dto.DssQueryDetailsDto;
import com.radiant.query.domain.dto.DssQueryDto;
import com.radiant.query.domain.dto.DssQueryImplDto;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public interface DssQueryService {
   DssQuery getDssQuery(Long id);

   DssQueryDto mapQueryToDto(DssQuery query);

   List<DssQueryDto> getAllQueries();

   DssQueryImplDto addImplementation(Long queryId, DssQueryImplDto implRequest, @Nullable MultipartFile jarFile);

   DssQueryDetailsDto getQueryDetails(Long queryId);

   List<DssQueryImplDto> getImplementations(Long queryId);

   DssQueryImplementation getImplementation(Long queryId, Long implId);

   DssQueryImplDto getQueryImplementation(Long queryId, Long implId);

   DssQueryImplDto updateImplementation(Long queryId, Long implId, DssQueryImplDto updateRequest, @Nullable MultipartFile jarFile);

   DssQueryImplDto updateImplStatus(Long queryId, Long implId, DssQueryImplDto updateRequest);

   void deleteImplementation(Long queryId, Long implId);

   DssQuery getDssQuery(String name);

   boolean processQueryUpdateEvent(GddsQueryEvent gddsQueryEvent);

   boolean syncQueries(List<Long> queryIds);
}
