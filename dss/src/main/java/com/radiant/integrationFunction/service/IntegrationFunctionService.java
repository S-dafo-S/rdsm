package com.radiant.integrationFunction.service;

import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.integrationFunction.domain.dto.IntegrationFunctionDto;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public interface IntegrationFunctionService {
   List<IntegrationFunctionDto> getAll();

   IntegrationFunction create(IntegrationFunctionDto request, MultipartFile jarFile);

   IntegrationFunctionDto createAndGet(IntegrationFunctionDto request, MultipartFile jarFile);

   IntegrationFunctionDto update(Long funcId, IntegrationFunctionDto request, @Nullable MultipartFile jarFile);

   void delete(Long funcId);

   IntegrationFunctionDto updateFile(Long funcId, MultipartFile jarFile);

   IntegrationFunction getById(Long id);
}
