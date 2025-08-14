package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class MultipleTopCourtException extends SystemException {
   private final String courtName;
   private final String regionName;

   public MultipleTopCourtException(String courtName, String regionName) {
      this.courtName = courtName;
      this.regionName = regionName;
   }

   public String getErrorCode() {
      return "TOP_COURT_ALREADY_EXIST_IN_REGION";
   }

   public String getCourtName() {
      return this.courtName;
   }

   public String getRegionName() {
      return this.regionName;
   }

   public String toString() {
      return "MultipleTopCourtException(courtName=" + this.getCourtName() + ", regionName=" + this.getRegionName() + ")";
   }
}
