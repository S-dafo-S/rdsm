package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class DssNotFoundForCourtException extends SystemException {
   public static final String CODE = "DNODE_NOT_FOUND_FOR_COURT";
   private Long courtId;

   public DssNotFoundForCourtException(Long courtId) {
      this.courtId = courtId;
   }

   public String getErrorCode() {
      return "DNODE_NOT_FOUND_FOR_COURT";
   }

   public Long getCourtId() {
      return this.courtId;
   }
}
