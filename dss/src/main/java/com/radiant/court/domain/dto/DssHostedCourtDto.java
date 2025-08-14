package com.radiant.court.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.court.domain.DssHostedCourt;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DssHostedCourtDto extends HostedCourDto {
   private DssCourtDto court;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private List<CourtDataStoreDto> courtDataStores = new ArrayList();
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date updateDate;

   public DssHostedCourtDto(DssHostedCourt hostedCourt) {
      super(hostedCourt);
      this.court = new DssCourtDto(hostedCourt.getCourt());
      this.courtDataStores = (List)hostedCourt.getDataStores().stream().map(CourtDataStoreDto::new).collect(Collectors.toList());
      this.creationDate = hostedCourt.getCreationDate();
      this.updateDate = hostedCourt.getUpdateDate();
   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public Date getUpdateDate() {
      return DateUtils.cloneDate(this.updateDate);
   }

   public DssCourtDto getCourt() {
      return this.court;
   }

   public List<CourtDataStoreDto> getCourtDataStores() {
      return this.courtDataStores;
   }

   public void setCourt(final DssCourtDto court) {
      this.court = court;
   }

   public DssHostedCourtDto() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DssHostedCourtDto)) {
         return false;
      } else {
         DssHostedCourtDto other = (DssHostedCourtDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            return super.equals(o);
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DssHostedCourtDto;
   }

   public int hashCode() {
      int result = super.hashCode();
      return result;
   }
}
