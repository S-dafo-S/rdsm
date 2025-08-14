package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class DuplicatePluginClassNameException extends SystemException {
   final String className;
   final String integrationFunctionName;

   public DuplicatePluginClassNameException(String className, String integrationFunctionName) {
      this.className = className;
      this.integrationFunctionName = integrationFunctionName;
   }

   public String getErrorCode() {
      return "DUPLICATE_PLUGIN_CLASS_NAME";
   }

   public String getErrorMessage() {
      return this.className;
   }

   public String toString() {
      return "DuplicatePluginClassNameException(className=" + this.className + ", integrationFunctionName=" + this.integrationFunctionName + ")";
   }
}
