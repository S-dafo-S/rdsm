package com.radiant.court.domain.dto;

import com.radiant.court.domain.DssHostedCourt;
import com.radiant.region.domain.DssRegion;

public class DssRegionCourtPair extends RegionCourtPair {
   private DssHostedCourtDto hostedCourt;

   public DssRegionCourtPair(DssRegion region, DssHostedCourt court, boolean hasChildren) {
      super(region, region.getParent() != null ? region.getParent().getId() : null, hasChildren);
      this.hostedCourt = new DssHostedCourtDto(court);
   }

   public DssHostedCourtDto getHostedCourt() {
      return this.hostedCourt;
   }

   public void setHostedCourt(final DssHostedCourtDto hostedCourt) {
      this.hostedCourt = hostedCourt;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DssRegionCourtPair)) {
         return false;
      } else {
         DssRegionCourtPair other = (DssRegionCourtPair)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$hostedCourt = this.getHostedCourt();
            Object other$hostedCourt = other.getHostedCourt();
            if (this$hostedCourt == null) {
               if (other$hostedCourt != null) {
                  return false;
               }
            } else if (!this$hostedCourt.equals(other$hostedCourt)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DssRegionCourtPair;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $hostedCourt = this.getHostedCourt();
      result = result * 59 + ($hostedCourt == null ? 43 : $hostedCourt.hashCode());
      return result;
   }

   public DssRegionCourtPair() {
   }
}
