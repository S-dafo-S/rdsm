package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.query.domain.GddsQuery;
import com.radiant.util.DateUtils;
import java.util.Date;

public class GddsQueryDetailsDto extends QueryDto {
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date updateDate;

   public GddsQueryDetailsDto(GddsQuery query) {
      super(query, query.getArguments());
      this.creationDate = query.getCreationDate();
      this.updateDate = query.getUpdateDate();
   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public Date getUpdateDate() {
      return DateUtils.cloneDate(this.updateDate);
   }

   public GddsQueryDetailsDto() {
   }
}
