package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class DuplicateRegionNameException extends SystemException {
   private final String name;

   public DuplicateRegionNameException(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_REGION_NAME";
   }
}
