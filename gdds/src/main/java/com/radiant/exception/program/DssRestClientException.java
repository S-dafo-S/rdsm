package com.radiant.exception.program;

import com.radiant.exception.SystemException;

public class DssRestClientException extends SystemException {
   private final String dssUrl;

   public DssRestClientException(Throwable cause, String dssUrl) {
      super(cause);
      this.dssUrl = dssUrl;
   }

   public String getErrorCode() {
      return "DNODE_CLIENT_CONNECTION_FAILED";
   }

   public String getDssUrl() {
      return this.dssUrl;
   }

   public String toString() {
      return "DssRestClientException(dssUrl=" + this.getDssUrl() + ")";
   }
}
