package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DssQueryDetailsDto extends QueryDto {
   private Date creationDate;
   private List<DssQueryImplDto> implementations = new ArrayList();
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private String downloadDocLink;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private String downloadSampleCodeLink;

   public DssQueryDetailsDto(DssQuery dssQuery, List<DssQueryImplementation> implementations, String gddsUrl) {
      super(dssQuery, dssQuery.getArguments());
      this.creationDate = dssQuery.getCreationDate();
      this.implementations = (List)implementations.stream().map(DssQueryImplDto::new).collect(Collectors.toList());
      if (gddsUrl != null) {
         this.downloadDocLink = gddsUrl + "/api/internal/v1/query/" + dssQuery.getName() + "/doc";
         this.downloadSampleCodeLink = gddsUrl + "/api/internal/v1/query/" + dssQuery.getName() + "/sample";
      }

   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public List<DssQueryImplDto> getImplementations() {
      return this.implementations;
   }

   public String getDownloadDocLink() {
      return this.downloadDocLink;
   }

   public String getDownloadSampleCodeLink() {
      return this.downloadSampleCodeLink;
   }

   public void setImplementations(final List<DssQueryImplDto> implementations) {
      this.implementations = implementations;
   }

   public DssQueryDetailsDto() {
   }
}
