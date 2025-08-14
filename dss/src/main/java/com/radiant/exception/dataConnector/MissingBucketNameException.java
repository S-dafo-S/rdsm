package com.radiant.exception.dataConnector;

import com.radiant.exception.SystemException;

public class MissingBucketNameException extends SystemException {
   public String getErrorCode() {
      return "BUCKET_NAME_IS_MISSING";
   }

   public String toString() {
      return "MissingBucketNameException()";
   }
}
