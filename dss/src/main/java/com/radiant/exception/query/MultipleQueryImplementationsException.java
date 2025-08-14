package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class MultipleQueryImplementationsException extends SystemException {
   private final String queryName;

   public MultipleQueryImplementationsException(String queryName) {
      this.queryName = queryName;
   }

   public String getErrorCode() {
      return "MULTIPLE_QUERY_IMPLEMENTATIONS";
   }

   public String getQueryName() {
      return this.queryName;
   }

   public String toString() {
      return "MultipleQueryImplementationsException(queryName=" + this.getQueryName() + ")";
   }
}
