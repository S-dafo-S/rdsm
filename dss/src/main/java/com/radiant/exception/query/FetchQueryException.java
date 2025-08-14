package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class FetchQueryException extends SystemException {
   public String getErrorCode() {
      return "FAILED_FETCH_QNODE_QUERY";
   }

   public String toString() {
      return "FetchQueryException()";
   }
}
