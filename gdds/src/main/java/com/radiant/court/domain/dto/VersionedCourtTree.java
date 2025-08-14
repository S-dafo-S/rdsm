package com.radiant.court.domain.dto;

import javax.annotation.Nullable;

public class VersionedCourtTree {
   String version;
   @Nullable
   CourtTreeNode courts;

   public VersionedCourtTree(String version) {
      this.version = version;
   }

   public String getVersion() {
      return this.version;
   }

   @Nullable
   public CourtTreeNode getCourts() {
      return this.courts;
   }

   public void setVersion(final String version) {
      this.version = version;
   }

   public void setCourts(@Nullable final CourtTreeNode courts) {
      this.courts = courts;
   }

   public VersionedCourtTree() {
   }
}
