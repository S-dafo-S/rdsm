package com.radiant.exception.program;

import com.radiant.exception.SystemException;

public class DuplicateProgramServiceNameException extends SystemException {
   final String serviceName;
   final String queryName;

   public DuplicateProgramServiceNameException(String serviceName, String queryName) {
      this.serviceName = serviceName;
      this.queryName = queryName;
   }

   public String getErrorCode() {
      return "DUPLICATE_PROGRAM_SERVICE_NAME";
   }

   public String getErrorMessage() {
      return this.serviceName;
   }

   public String toString() {
      return "DuplicateProgramServiceNameException(serviceName=" + this.serviceName + ", queryName=" + this.queryName + ")";
   }
}
