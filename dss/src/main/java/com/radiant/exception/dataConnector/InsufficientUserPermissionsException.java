package com.radiant.exception.dataConnector;

import com.radiant.exception.SystemException;

public class InsufficientUserPermissionsException extends SystemException {
   private final String message;

   public InsufficientUserPermissionsException() {
      this.message = "Insufficient user permissions. Please contact the administrator.";
   }

   public InsufficientUserPermissionsException(String message) {
      this.message = message;
   }

   public String getErrorCode() {
      return "INSUFFICIENT_USER_PERMISSIONS";
   }

   public String getErrorMessage() {
      return this.message;
   }

   public String toString() {
      return "InsufficientUserPermissionsException(message=" + this.message + ")";
   }
}
