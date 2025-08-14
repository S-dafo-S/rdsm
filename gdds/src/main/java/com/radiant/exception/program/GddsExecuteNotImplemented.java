package com.radiant.exception.program;

import com.radiant.exception.NotImplementedException;

public class GddsExecuteNotImplemented extends NotImplementedException {
   public GddsExecuteNotImplemented(String message) {
      super(message);
   }

   public String getErrorCode() {
      return "QNODE_EXECUTE_METHOD_NOT_IMPLEMENTED";
   }

   public String toString() {
      return "GddsExecuteNotImplemented()";
   }
}
