package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class CourtVersionException extends SystemException {
   private final String version;

   public CourtVersionException(String version) {
      this.version = version;
   }

   public String getErrorCode() {
      return "INVALID_COURT_VERSION";
   }

   public String getVersion() {
      return this.version;
   }

   public String toString() {
      return "CourtVersionException(version=" + this.getVersion() + ")";
   }
}
