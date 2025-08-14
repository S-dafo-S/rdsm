package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class UnknownQueryArgument extends SystemException {
   private final String argName;

   public UnknownQueryArgument(String argName) {
      this.argName = argName;
   }

   public String getErrorCode() {
      return "UNKNOWN_QUERY_IMPLEMENTATION_ARGUMENT";
   }

   public String getArgName() {
      return this.argName;
   }

   public String toString() {
      return "UnknownQueryArgument(argName=" + this.getArgName() + ")";
   }
}
