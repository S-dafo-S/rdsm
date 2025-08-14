package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class DuplicateCourtName extends SystemException {
   private final String name;

   public DuplicateCourtName(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_COURT_NAME";
   }
}
