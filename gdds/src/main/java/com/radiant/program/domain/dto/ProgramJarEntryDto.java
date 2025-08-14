package com.radiant.program.domain.dto;

import com.radiant.program.dto.IntegrationType;
import com.radiant.program.dto.ProgramMethod;
import com.radiant.query.registry.JarEntryDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ProgramJarEntryDto {
   Collection<IntegrationType> integrationTypes = new HashSet();
   ProgramMethod method;
   List<JarEntryDto> entries = new ArrayList();

   public ProgramJarEntryDto(Collection<IntegrationType> integrationTypes, ProgramMethod method, List<JarEntryDto> entries) {
      this.integrationTypes = integrationTypes;
      this.method = method;
      this.entries = entries;
   }

   public ProgramJarEntryDto() {
   }

   public Collection<IntegrationType> getIntegrationTypes() {
      return this.integrationTypes;
   }

   public ProgramMethod getMethod() {
      return this.method;
   }

   public List<JarEntryDto> getEntries() {
      return this.entries;
   }

   public void setIntegrationTypes(final Collection<IntegrationType> integrationTypes) {
      this.integrationTypes = integrationTypes;
   }

   public void setMethod(final ProgramMethod method) {
      this.method = method;
   }

   public void setEntries(final List<JarEntryDto> entries) {
      this.entries = entries;
   }
}
