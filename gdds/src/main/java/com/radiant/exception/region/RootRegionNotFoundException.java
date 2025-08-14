package com.radiant.exception.region;

import com.radiant.exception.SystemException;

public class RootRegionNotFoundException extends SystemException {
   public String getErrorCode() {
      return "ROOT_REGION_NOT_FOUND";
   }

   public String toString() {
      return "RootRegionNotFoundException()";
   }
}
