package com.radiant.court.domain.dto;

import com.radiant.court.domain.DssCourt;

public class DssCourtDto extends CourtDto {
   public DssCourtDto(DssCourt court) {
      super(court, court.getRegion(), court.getRegion().getParent() != null ? court.getRegion().getParent().getId() : null);
   }

   public DssCourtDto(Long id, String name) {
      super(id, name);
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DssCourtDto)) {
         return false;
      } else {
         DssCourtDto other = (DssCourtDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            return super.equals(o);
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DssCourtDto;
   }

   public int hashCode() {
      int result = super.hashCode();
      return result;
   }

   public DssCourtDto() {
   }
}
