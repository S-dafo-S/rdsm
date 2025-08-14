package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class DuplicateCourtLocalId extends SystemException {
   private final Long localId;

   public DuplicateCourtLocalId(Long localId) {
      this.localId = localId;
   }

   public String getErrorCode() {
      return "DUPLICATE_COURT_LOCAL_ID";
   }

   public Long getLocalId() {
      return this.localId;
   }

   public String toString() {
      return "DuplicateCourtLocalId(localId=" + this.getLocalId() + ")";
   }
}
