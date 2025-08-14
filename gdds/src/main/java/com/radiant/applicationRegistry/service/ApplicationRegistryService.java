package com.radiant.applicationRegistry.service;

import com.radiant.applicationRegistry.domain.ApplicationRegistryDto;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public interface ApplicationRegistryService {
   ApplicationRegistryDto get(Long id);

   List<ApplicationRegistryDto> getAll();

   ApplicationRegistryDto create(ApplicationRegistryDto request);

   ApplicationRegistryDto update(Long id, ApplicationRegistryDto request);

   void delete(Long id);

   void validateApiAccess(HttpServletRequest request, String apiName);

   void validateDssAccess(HttpServletRequest request, Set<Long> dssIds);
}
