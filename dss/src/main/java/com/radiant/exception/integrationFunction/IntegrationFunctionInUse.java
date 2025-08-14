package com.radiant.exception.integrationFunction;

import com.radiant.exception.SystemException;

public class IntegrationFunctionInUse extends SystemException {
   private final String name;

   public IntegrationFunctionInUse(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "INTEGRATION_FUNCTION_IN_USE";
   }

   public String getName() {
      return this.name;
   }

   public String toString() {
      return "IntegrationFunctionInUse(name=" + this.getName() + ")";
   }
}
