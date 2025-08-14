package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class QueryPluginExecutionException extends SystemException {
   public QueryPluginExecutionException(Throwable cause) {
      super(cause);
   }

   public String getErrorCode() {
      return "QUERY_PLUGIN_EXECUTION_FAILED";
   }
}
