package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class InvalidCourtLevelInRegion extends SystemException {
   private final String courtName;
   private final Long courtLevel;
   private final Long regionLevel;
   private final InvalidCourtLevelMessageCode code;

   public InvalidCourtLevelInRegion(String courtName, Long courtLevel, Long regionLevel, InvalidCourtLevelMessageCode code) {
      this.courtName = courtName;
      this.courtLevel = courtLevel;
      this.regionLevel = regionLevel;
      this.code = code;
   }

   public String getErrorCode() {
      return this.code.name();
   }

   public String getErrorMessage() {
      return this.courtName;
   }

   public String getCourtName() {
      return this.courtName;
   }

   public Long getCourtLevel() {
      return this.courtLevel;
   }

   public Long getRegionLevel() {
      return this.regionLevel;
   }

   public InvalidCourtLevelMessageCode getCode() {
      return this.code;
   }

   public String toString() {
      return "InvalidCourtLevelInRegion(courtName=" + this.getCourtName() + ", courtLevel=" + this.getCourtLevel() + ", regionLevel=" + this.getRegionLevel() + ", code=" + this.getCode() + ")";
   }

   public static enum InvalidCourtLevelMessageCode {
      COURT_LEVEL_MUST_BE_GREAT_EQUAL_REGION_LEVEL,
      GREATER_COURT_LEVEL_FOR_NOT_MAX_LEVEL_REGION;
   }
}
