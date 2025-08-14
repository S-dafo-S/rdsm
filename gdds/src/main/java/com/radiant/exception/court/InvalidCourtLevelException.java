package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class InvalidCourtLevelException extends SystemException {
   private final String name;
   private final Long level;

   public InvalidCourtLevelException(String name, Long level) {
      this.name = name;
      this.level = level;
   }

   public String getErrorCode() {
      return "INVALID_COURT_LEVEL";
   }

   public String toString() {
      return "InvalidCourtLevelException(name=" + this.getName() + ", level=" + this.getLevel() + ")";
   }

   public String getName() {
      return this.name;
   }

   public Long getLevel() {
      return this.level;
   }
}
