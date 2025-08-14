package com.radiant.exception.query;

import com.radiant.exception.SystemException;

public class PaginationIsNotAvailableException extends SystemException {
   public String getErrorCode() {
      return "PAGINATION_IS_NOT_AVAILABLE";
   }

   public String getErrorMessage() {
      return "Query result is not an array, pagination is not available";
   }
}
