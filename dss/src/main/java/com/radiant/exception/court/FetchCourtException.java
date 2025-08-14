package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class FetchCourtException extends SystemException {
   private final Long courtId;

   public FetchCourtException(Long courtId) {
      this.courtId = courtId;
   }

   public String getErrorCode() {
      return "FETCH_COURT_EXCEPTION";
   }

   public Long getCourtId() {
      return this.courtId;
   }

   public String toString() {
      return "FetchCourtException(courtId=" + this.getCourtId() + ")";
   }
}
