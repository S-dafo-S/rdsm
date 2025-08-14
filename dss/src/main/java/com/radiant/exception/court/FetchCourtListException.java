package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class FetchCourtListException extends SystemException {
   public String getErrorCode() {
      return "FETCH_COURT_LIST_EXCEPTION";
   }

   public String toString() {
      return "FetchCourtListException()";
   }
}
