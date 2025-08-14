package com.radiant.court.domain.dto;

import com.radiant.court.domain.GddsCourt;
import com.radiant.region.domain.GddsRegion;
import com.radiant.region.domain.dto.RegionDto;

public class GddsCourtDto extends CourtDto {
   private String contactName;
   private String contactPhone;
   private String contactEmail;
   private String description;
   private String shortName;

   public GddsCourtDto(GddsCourt court) {
      super(court.getId(), court.getName());
      this.setLevel(court.getLevel());
      if (court.getParentRegion() != null) {
         GddsRegion r = court.getParentRegion().getRegion();
         this.setRegion(new RegionDto(r, r.getParent() != null ? r.getParent().getId() : null));
      }

      this.contactName = court.getContactName();
      this.contactPhone = court.getContactPhone();
      this.contactEmail = court.getContactEmail();
      this.description = court.getDescription();
      this.shortName = court.getShortName();
   }

   public String getContactName() {
      return this.contactName;
   }

   public String getContactPhone() {
      return this.contactPhone;
   }

   public String getContactEmail() {
      return this.contactEmail;
   }

   public String getDescription() {
      return this.description;
   }

   public String getShortName() {
      return this.shortName;
   }

   public void setContactName(final String contactName) {
      this.contactName = contactName;
   }

   public void setContactPhone(final String contactPhone) {
      this.contactPhone = contactPhone;
   }

   public void setContactEmail(final String contactEmail) {
      this.contactEmail = contactEmail;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public void setShortName(final String shortName) {
      this.shortName = shortName;
   }

   public GddsCourtDto() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GddsCourtDto)) {
         return false;
      } else {
         GddsCourtDto other = (GddsCourtDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            return super.equals(o);
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof GddsCourtDto;
   }

   public int hashCode() {
      int result = super.hashCode();
      return result;
   }
}
