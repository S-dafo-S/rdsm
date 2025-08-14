package com.radiant.region.domain.dto;

public class RegionStatusDto {
   private OutdatedStatus status;

   public RegionStatusDto(OutdatedStatus status) {
      this.status = status;
   }

   public OutdatedStatus getStatus() {
      return this.status;
   }
}
