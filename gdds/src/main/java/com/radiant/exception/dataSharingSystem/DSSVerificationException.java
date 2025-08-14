package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;
import org.apache.commons.lang3.StringUtils;

public class DSSVerificationException extends SystemException {
   private final String baseUrl;
   private final String reason;

   public DSSVerificationException(String baseUrl, String reason) {
      this.baseUrl = baseUrl;
      this.reason = reason;
   }

   public String getErrorCode() {
      return "DNODE_VERIFICATION_FAILED";
   }

   public String getErrorMessage() {
      return "DNode URL: " + this.baseUrl + (StringUtils.isNotEmpty(this.reason) ? "; " + this.reason : "");
   }

   public String getBaseUrl() {
      return this.baseUrl;
   }

   public String getReason() {
      return this.reason;
   }

   public String toString() {
      return "DSSVerificationException(baseUrl=" + this.getBaseUrl() + ", reason=" + this.getReason() + ")";
   }
}
