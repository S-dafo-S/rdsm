package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class MultipleRootRegionException extends SystemException {
   public String getErrorCode() {
      return "MULTIPLE_ROOT_REGIONS";
   }

   public String toString() {
      return "MultipleRootRegionException()";
   }
}
