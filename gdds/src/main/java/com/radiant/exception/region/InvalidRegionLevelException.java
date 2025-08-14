package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class InvalidRegionLevelException extends SystemException {
   private final String name;
   private final Long level;

   public InvalidRegionLevelException(String name, Long level) {
      this.name = name;
      this.level = level;
   }

   public String getErrorCode() {
      return "INVALID_REGION_LEVEL";
   }

   public String toString() {
      return "InvalidRegionLevelException(name=" + this.getName() + ", level=" + this.getLevel() + ")";
   }

   public String getName() {
      return this.name;
   }

   public Long getLevel() {
      return this.level;
   }
}
