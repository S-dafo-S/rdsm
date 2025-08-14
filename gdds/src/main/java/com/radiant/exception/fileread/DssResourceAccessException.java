package com.radiant.exception.fileread;

import com.radiant.exception.SystemException;

public class DssResourceAccessException extends SystemException {
   private final String dssName;
   private final String baseUrl;

   public DssResourceAccessException(Throwable cause, String dssName, String baseUrl) {
      super(cause);
      this.dssName = dssName;
      this.baseUrl = baseUrl;
   }

   public String getErrorCode() {
      return "DNODE_RESOURCE_ACCESS_FAILED";
   }

   public String toString() {
      return "DssResourceAccessException(dssName=" + this.dssName + ", baseUrl=" + this.baseUrl + ")";
   }
}
