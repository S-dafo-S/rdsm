package com.radiant.exception.program;

import com.radiant.exception.SystemException;

public class NotSupportedProgramMethodException extends SystemException {
   private final String method;

   public NotSupportedProgramMethodException(String method) {
      this.method = method;
   }

   public String getErrorCode() {
      return "NOT_SUPPORTED_PROGRAM_METHOD";
   }

   public String toString() {
      return "NotSupportedProgramMethodException(method=" + this.method + ")";
   }
}
