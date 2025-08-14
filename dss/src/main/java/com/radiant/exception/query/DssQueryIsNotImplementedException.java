package com.radiant.exception.query;

import com.radiant.account.exception.NotFoundException;
import javax.annotation.Nullable;

public class DssQueryIsNotImplementedException extends NotFoundException {
   public DssQueryIsNotImplementedException(@Nullable String name) {
      super(name);
   }

   public String getErrorCode() {
      return "QUERY_IS_NOT_IMPLEMENTED_FOR_DATA_CONNECTOR";
   }

   public String toString() {
      return "DssQueryIsNotImplementedException()";
   }
}
