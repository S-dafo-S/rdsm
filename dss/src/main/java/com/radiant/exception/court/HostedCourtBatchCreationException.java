package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class HostedCourtBatchCreationException extends SystemException {
   final String messageCode;

   public HostedCourtBatchCreationException(CourtBatchCreationMessageCode code) {
      this.messageCode = code.name();
   }

   public String getErrorCode() {
      return this.messageCode;
   }

   public String getMessageCode() {
      return this.messageCode;
   }

   public String toString() {
      return "HostedCourtBatchCreationException(messageCode=" + this.getMessageCode() + ")";
   }

   public static enum CourtBatchCreationMessageCode {
      SOME_OF_COURTS_ALREADY_HOSTED,
      SOME_OF_COURT_LOCAL_ID_IS_DUPLICATE;
   }
}
