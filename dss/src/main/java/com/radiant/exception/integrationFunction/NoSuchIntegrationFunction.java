package com.radiant.exception.integrationFunction;

import com.radiant.account.exception.NotFoundException;
import org.jetbrains.annotations.Nullable;

public class NoSuchIntegrationFunction extends NotFoundException {
   public NoSuchIntegrationFunction(@Nullable Long id) {
      super(id);
   }

   public String getErrorCode() {
      return "NO_SUCH_INTEGRATION_FUNCTION";
   }

   public String toString() {
      return "NoSuchIntegrationFunction()";
   }
}
