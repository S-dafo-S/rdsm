package com.radiant.integrationFunction.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.dto.NameId;
import com.radiant.integrationFunction.domain.IntegrationFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntegrationFunctionShortDto {
   private Long id;
   private String originalFilename;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private List<NameId> usedBy = new ArrayList();

   public IntegrationFunctionShortDto(IntegrationFunction function) {
      this.id = function.getId();
      this.originalFilename = function.getOriginalFilename();
      this.usedBy = (List)function.getUsedBy().stream().map((impl) -> new NameId(impl.getId(), impl.getName())).collect(Collectors.toList());
   }

   public Long getId() {
      return this.id;
   }

   public String getOriginalFilename() {
      return this.originalFilename;
   }

   public List<NameId> getUsedBy() {
      return this.usedBy;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setOriginalFilename(final String originalFilename) {
      this.originalFilename = originalFilename;
   }

   public IntegrationFunctionShortDto() {
   }
}
