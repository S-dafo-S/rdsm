package com.radiant.exception.integrationFunction;

import com.radiant.exception.SystemException;

public class DuplicateIntegrationFunctionName extends SystemException {
   private final String name;

   public DuplicateIntegrationFunctionName(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_INTEGRATION_FUNCTION_NAME";
   }

   public String getErrorMessage() {
      return this.name;
   }

   public String getName() {
      return this.name;
   }

   public String toString() {
      return "DuplicateIntegrationFunctionName(name=" + this.getName() + ")";
   }
}
