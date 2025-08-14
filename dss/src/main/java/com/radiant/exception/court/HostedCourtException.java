package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class HostedCourtException extends SystemException {
   private final Long courtId;

   public HostedCourtException(Long courtId) {
      this.courtId = courtId;
   }

   public String getErrorCode() {
      return "COURT_IS_HOSTED";
   }

   public Long getCourtId() {
      return this.courtId;
   }

   public String toString() {
      return "HostedCourtException(courtId=" + this.getCourtId() + ")";
   }
}
