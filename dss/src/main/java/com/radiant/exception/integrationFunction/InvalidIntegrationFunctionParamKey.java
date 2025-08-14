package com.radiant.exception.integrationFunction;

import com.radiant.exception.SystemException;

public class InvalidIntegrationFunctionParamKey extends SystemException {
   private final String key;

   public InvalidIntegrationFunctionParamKey(String key) {
      this.key = key;
   }

   public String getErrorCode() {
      return "INVALID_INTEGRATION_FUNCTION_PARAM_KEY";
   }

   public String toString() {
      return "InvalidIntegrationFunctionParamKey(key=" + this.getKey() + ")";
   }

   public String getKey() {
      return this.key;
   }
}
