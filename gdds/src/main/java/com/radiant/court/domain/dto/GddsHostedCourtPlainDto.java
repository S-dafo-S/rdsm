package com.radiant.court.domain.dto;

import com.radiant.court.domain.GddsHostedCourt;

public class GddsHostedCourtPlainDto {
   private Long id;
   private Long courtId;
   private Long dssId;

   public GddsHostedCourtPlainDto(GddsHostedCourt hostedCourt) {
      this.id = hostedCourt.getId();
      this.courtId = hostedCourt.getCourt().getId();
      this.dssId = hostedCourt.getDss().getId();
   }

   public Long getId() {
      return this.id;
   }

   public Long getCourtId() {
      return this.courtId;
   }

   public Long getDssId() {
      return this.dssId;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setCourtId(final Long courtId) {
      this.courtId = courtId;
   }

   public void setDssId(final Long dssId) {
      this.dssId = dssId;
   }

   public GddsHostedCourtPlainDto() {
   }
}
