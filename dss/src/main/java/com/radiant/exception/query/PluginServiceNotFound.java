package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class PluginServiceNotFound extends SystemException {
   final String expectedServiceName;

   public PluginServiceNotFound(String expectedServiceName) {
      this.expectedServiceName = expectedServiceName;
   }

   public String getErrorCode() {
      return "EXPECTED_SERVICE_NOT_FOUND_IN_PLUGIN_JAR";
   }

   public String getErrorMessage() {
      return this.expectedServiceName;
   }

   public String toString() {
      return "PluginServiceNotFound(expectedServiceName=" + this.expectedServiceName + ")";
   }
}
