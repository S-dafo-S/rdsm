package com.radiant.exception.integrationFunction;

import com.radiant.exception.SystemException;

public class InvalidIntegrationFunctionConnectorKey extends SystemException {
   private final String key;

   public InvalidIntegrationFunctionConnectorKey(String name) {
      this.key = name;
   }

   public String getErrorCode() {
      return "INVALID_INTEGRATION_FUNCTION_CONNECTOR_KEY";
   }

   public String getKey() {
      return this.key;
   }

   public String toString() {
      return "InvalidIntegrationFunctionConnectorKey(key=" + this.getKey() + ")";
   }
}
