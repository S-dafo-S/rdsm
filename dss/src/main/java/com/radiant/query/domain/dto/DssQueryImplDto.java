package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.integrationFunction.domain.dto.IntegrationFunctionShortDto;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.JavaQueryImplementation;
import com.radiant.query.domain.SqlQueryImplementation;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DssQueryImplDto {
   private Long id;
   private String name;
   private List<QueryImplConnectorDto> namedConnectors = new ArrayList();
   private QueryLanguage lang;
   private Boolean isActive;
   private String code;
   private List<QueryParamDto> parameters = new ArrayList();
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date activationDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date updateDate;
   private IntegrationFunctionShortDto integrationFunction;

   public DssQueryImplDto(DssQueryImplementation implementation) {
      this.id = implementation.getId();
      this.name = implementation.getName();
      this.namedConnectors = (List)implementation.getQueryImplDataConnectors().stream().map(QueryImplConnectorDto::new).collect(Collectors.toList());
      this.isActive = implementation.getIsActive();
      this.parameters = (List)implementation.getParameters().entrySet().stream().map(QueryParamDto::new).collect(Collectors.toList());
      this.activationDate = implementation.getActivationDate();
      this.creationDate = implementation.getCreationDate();
      this.updateDate = implementation.getUpdateDate();
      if (implementation instanceof SqlQueryImplementation) {
         this.lang = QueryLanguage.SQL;
         this.code = ((SqlQueryImplementation)implementation).getCode();
      } else if (implementation instanceof JavaQueryImplementation) {
         this.lang = QueryLanguage.JAVA;
         IntegrationFunction intFunction = ((JavaQueryImplementation)implementation).getIntegrationFunction();
         if (intFunction != null) {
            this.integrationFunction = new IntegrationFunctionShortDto(intFunction);
         }
      }

   }

   public Date getActivationDate() {
      return DateUtils.cloneDate(this.activationDate);
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

   public List<QueryImplConnectorDto> getNamedConnectors() {
      return this.namedConnectors;
   }

   public QueryLanguage getLang() {
      return this.lang;
   }

   public Boolean getIsActive() {
      return this.isActive;
   }

   public String getCode() {
      return this.code;
   }

   public List<QueryParamDto> getParameters() {
      return this.parameters;
   }

   public IntegrationFunctionShortDto getIntegrationFunction() {
      return this.integrationFunction;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setNamedConnectors(final List<QueryImplConnectorDto> namedConnectors) {
      this.namedConnectors = namedConnectors;
   }

   public void setLang(final QueryLanguage lang) {
      this.lang = lang;
   }

   public void setIsActive(final Boolean isActive) {
      this.isActive = isActive;
   }

   public void setCode(final String code) {
      this.code = code;
   }

   public void setParameters(final List<QueryParamDto> parameters) {
      this.parameters = parameters;
   }

   public void setIntegrationFunction(final IntegrationFunctionShortDto integrationFunction) {
      this.integrationFunction = integrationFunction;
   }

   public DssQueryImplDto() {
   }
}
