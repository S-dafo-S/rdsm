package com.radiant.applicationRegistry;

import com.radiant.applicationRegistry.domain.ApplicationRegistryDto;
import com.radiant.applicationRegistry.service.ApplicationRegistryService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/internal/v1/application_registry"})
public class ApplicationRegistryController {
   @Autowired
   private ApplicationRegistryService service;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   public List<ApplicationRegistryDto> getAll() {
      return this.service.getAll();
   }

   @GetMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   public ApplicationRegistryDto get(@PathVariable("id") Long id) {
      return this.service.get(id);
   }

   @PostMapping
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   @ResponseStatus(HttpStatus.CREATED)
   public ApplicationRegistryDto create(@RequestBody ApplicationRegistryDto request) {
      return this.service.create(request);
   }

   @PutMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   public ApplicationRegistryDto update(@PathVariable("id") Long id, @RequestBody ApplicationRegistryDto request) {
      return this.service.update(id, request);
   }

   @DeleteMapping({"/{id}"})
   @PreAuthorize("hasAnyAuthority('GDDS_SYSADMIN')")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void delete(@PathVariable("id") Long id) {
      this.service.delete(id);
   }
}
