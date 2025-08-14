package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class DuplicateCourtId extends SystemException {
   private final Long id;

   public DuplicateCourtId(Long id) {
      this.id = id;
   }

   public String getErrorCode() {
      return "DUPLICATE_COURT_ID";
   }

   public Long getId() {
      return this.id;
   }

   public String toString() {
      return "DuplicateCourtId(id=" + this.getId() + ")";
   }
}
