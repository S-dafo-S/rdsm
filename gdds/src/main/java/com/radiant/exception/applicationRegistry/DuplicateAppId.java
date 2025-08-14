package com.radiant.exception.applicationRegistry;

import com.radiant.exception.SystemException;

public class DuplicateAppId extends SystemException {
   private final String name;

   public DuplicateAppId(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_APP_ID";
   }
}
