package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;
import org.apache.commons.lang3.StringUtils;

public class DSSConnectionException extends SystemException {
   private String dssName;
   private String baseUrl;

   public DSSConnectionException(String dssName, String baseUrl) {
      this.dssName = dssName;
      this.baseUrl = baseUrl;
   }

   public DSSConnectionException(String baseUrl) {
      this.baseUrl = baseUrl;
   }

   public String getErrorCode() {
      return "DNODE_CONNECTION_FAILED";
   }

   public String getErrorMessage() {
      return "DNode URL: " + this.baseUrl + (StringUtils.isNotEmpty(this.dssName) ? "; DNode NAME: " + this.dssName : "");
   }

   public String getDssName() {
      return this.dssName;
   }

   public String getBaseUrl() {
      return this.baseUrl;
   }

   public String toString() {
      return "DSSConnectionException(dssName=" + this.getDssName() + ", baseUrl=" + this.getBaseUrl() + ")";
   }
}
