package com.radiant.exception.court;

import com.radiant.exception.SystemException;

public class UnknownDeploymentCourtException extends SystemException {
   public String getErrorCode() {
      return "UNKNOWN_DEPLOYMENT_COURT";
   }

   public String toString() {
      return "UnknownDeploymentCourtException()";
   }
}
