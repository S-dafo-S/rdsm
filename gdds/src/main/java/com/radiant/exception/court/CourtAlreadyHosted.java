package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class CourtAlreadyHosted extends SystemException {
   private final Long sourceCourtId;

   public CourtAlreadyHosted(Long sourceCourtId) {
      this.sourceCourtId = sourceCourtId;
   }

   public String getErrorCode() {
      return "COURT_ALREADY_HOSTED";
   }

   public String toString() {
      return "CourtAlreadyHosted(sourceCourtId=" + this.sourceCourtId + ")";
   }
}
