package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class DuplicateRegionIdException extends SystemException {
   private final Long id;

   public DuplicateRegionIdException(Long id) {
      this.id = id;
   }

   public String getErrorCode() {
      return "DUPLICATE_REGION_ID";
   }
}
