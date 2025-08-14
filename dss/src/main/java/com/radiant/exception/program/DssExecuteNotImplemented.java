package com.radiant.exception.program;

import com.radiant.exception.NotImplementedException;

public class DssExecuteNotImplemented extends NotImplementedException {
   public DssExecuteNotImplemented(String message) {
      super(message);
   }

   public String getErrorCode() {
      return "DNODE_EXECUTE_METHOD_NOT_IMPLEMENTED";
   }

   public String toString() {
      return "DssExecuteNotImplemented()";
   }
}
