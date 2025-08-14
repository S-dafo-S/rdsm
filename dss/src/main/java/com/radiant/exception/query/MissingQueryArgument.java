package com.radiant.exception.query;

import com.radiant.exception.SystemException;
import javax.validation.constraints.NotNull;

public class MissingQueryArgument extends SystemException {
   private final @NotNull String argName;

   public MissingQueryArgument(String argName) {
      this.argName = argName;
   }

   public String getErrorCode() {
      return "ARGUMENT_VALUE_IS_MISSING";
   }

   public String getErrorMessage() {
      return "Missing argument: " + this.argName;
   }

   public String getArgName() {
      return this.argName;
   }

   public String toString() {
      return "MissingQueryArgument(argName=" + this.getArgName() + ")";
   }
}
