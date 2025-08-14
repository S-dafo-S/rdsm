package com.radiant.court.domain.dto;

import com.radiant.court.domain.GddsCourt;

public class GddsCourtPlainDto {
   private Long courtId;
   private String name;
   private Long level;
   private Long region;
   private String shortName;

   public GddsCourtPlainDto(GddsCourt court) {
      this.courtId = court.getId();
      this.name = court.getName();
      this.level = court.getLevel();
      this.region = court.getParentRegion() != null && court.getParentRegion().getRegion() != null ? court.getParentRegion().getRegion().getId() : null;
      this.shortName = court.getShortName();
   }

   public Long getCourtId() {
      return this.courtId;
   }

   public String getName() {
      return this.name;
   }

   public Long getLevel() {
      return this.level;
   }

   public Long getRegion() {
      return this.region;
   }

   public String getShortName() {
      return this.shortName;
   }

   public void setCourtId(final Long courtId) {
      this.courtId = courtId;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setLevel(final Long level) {
      this.level = level;
   }

   public void setRegion(final Long region) {
      this.region = region;
   }

   public void setShortName(final String shortName) {
      this.shortName = shortName;
   }

   public GddsCourtPlainDto() {
   }
}
