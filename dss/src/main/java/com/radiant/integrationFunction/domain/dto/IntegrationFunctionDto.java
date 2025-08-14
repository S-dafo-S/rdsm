package com.radiant.integrationFunction.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.dto.NameId;
import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.query.domain.JavaQueryImplementation;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class IntegrationFunctionDto {
   private Long id;
   private String name;
   private String description;
   private String originalFilename;
   private List<IntegrationFunctionConnectorDto> connectors = new ArrayList();
   private List<String> params = new ArrayList();
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date updateDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private List<NameId> usedBy = new ArrayList();

   public IntegrationFunctionDto(IntegrationFunction function) {
      this.id = function.getId();
      this.name = function.getName();
      this.description = function.getDescription();
      this.originalFilename = function.getOriginalFilename();
      this.connectors = (List)function.getConnectors().stream().map(IntegrationFunctionConnectorDto::new).collect(Collectors.toList());
      this.params = function.getParameters();
      this.creationDate = function.getCreationDate();
      this.updateDate = function.getUpdateDate();
      this.usedBy = (List)function.getUsedBy().stream().map((impl) -> new NameId(impl.getId(), impl.getName())).collect(Collectors.toList());
   }

   public IntegrationFunctionDto(JavaQueryImplementation impl, String filename) {
      this.name = impl.getName() + "_" + filename;
      this.originalFilename = filename;
      this.connectors = (List)impl.getQueryImplDataConnectors().stream().map(IntegrationFunctionConnectorDto::new).collect(Collectors.toList());

      for(String key : impl.getParameters().keySet()) {
         this.getParams().add(key);
      }

   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public Date getUpdateDate() {
      return DateUtils.cloneDate(this.updateDate);
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getOriginalFilename() {
      return this.originalFilename;
   }

   public List<IntegrationFunctionConnectorDto> getConnectors() {
      return this.connectors;
   }

   public List<String> getParams() {
      return this.params;
   }

   public List<NameId> getUsedBy() {
      return this.usedBy;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public void setOriginalFilename(final String originalFilename) {
      this.originalFilename = originalFilename;
   }

   public void setConnectors(final List<IntegrationFunctionConnectorDto> connectors) {
      this.connectors = connectors;
   }

   public void setParams(final List<String> params) {
      this.params = params;
   }

   public IntegrationFunctionDto() {
   }
}
