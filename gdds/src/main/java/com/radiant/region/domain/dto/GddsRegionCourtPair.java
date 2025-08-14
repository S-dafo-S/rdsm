package com.radiant.region.domain.dto;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.dto.GddsCourtDto;
import com.radiant.court.domain.dto.RegionCourtPair;
import com.radiant.region.domain.GddsRegion;

public class GddsRegionCourtPair extends RegionCourtPair {
   private GddsCourtDto court;
   private Long accessAll;
   private Long accessFail;

   public GddsRegionCourtPair(GddsRegion region, GddsCourt court, boolean hasChildren) {
      super(region, region.getParent() != null ? region.getParent().getId() : null, hasChildren);
      if (court != null) {
         this.court = new GddsCourtDto(court);
      }

   }

   public boolean isTopCourtPair() {
      return this.getCourt() == null || this.getLevel().equals(this.getCourt().getLevel());
   }

   public boolean isNotTopCourtPair() {
      return this.getCourt() != null && this.getCourt().getLevel() > this.getLevel();
   }

   public GddsCourtDto getCourt() {
      return this.court;
   }

   public Long getAccessAll() {
      return this.accessAll;
   }

   public Long getAccessFail() {
      return this.accessFail;
   }

   public void setCourt(final GddsCourtDto court) {
      this.court = court;
   }

   public void setAccessAll(final Long accessAll) {
      this.accessAll = accessAll;
   }

   public void setAccessFail(final Long accessFail) {
      this.accessFail = accessFail;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GddsRegionCourtPair)) {
         return false;
      } else {
         GddsRegionCourtPair other = (GddsRegionCourtPair)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$accessAll = this.getAccessAll();
            Object other$accessAll = other.getAccessAll();
            if (this$accessAll == null) {
               if (other$accessAll != null) {
                  return false;
               }
            } else if (!this$accessAll.equals(other$accessAll)) {
               return false;
            }

            Object this$accessFail = this.getAccessFail();
            Object other$accessFail = other.getAccessFail();
            if (this$accessFail == null) {
               if (other$accessFail != null) {
                  return false;
               }
            } else if (!this$accessFail.equals(other$accessFail)) {
               return false;
            }

            Object this$court = this.getCourt();
            Object other$court = other.getCourt();
            if (this$court == null) {
               if (other$court != null) {
                  return false;
               }
            } else if (!this$court.equals(other$court)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof GddsRegionCourtPair;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $accessAll = this.getAccessAll();
      result = result * 59 + ($accessAll == null ? 43 : $accessAll.hashCode());
      Object $accessFail = this.getAccessFail();
      result = result * 59 + ($accessFail == null ? 43 : $accessFail.hashCode());
      Object $court = this.getCourt();
      result = result * 59 + ($court == null ? 43 : $court.hashCode());
      return result;
   }

   public GddsRegionCourtPair() {
   }
}
