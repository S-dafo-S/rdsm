package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class MissingParentRegionException extends SystemException {
   private final String name;

   public MissingParentRegionException(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "MISSING_PARENT_REGION";
   }

   public String toString() {
      return "MissingParentRegionException(name=" + this.getName() + ")";
   }

   public String getName() {
      return this.name;
   }
}
