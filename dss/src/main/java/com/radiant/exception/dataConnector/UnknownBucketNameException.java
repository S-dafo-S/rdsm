package com.radiant.exception.dataConnector;

import com.radiant.exception.SystemException;

public class UnknownBucketNameException extends SystemException {
   private final String bucketName;

   public UnknownBucketNameException(String bucketName) {
      this.bucketName = bucketName;
   }

   public String getErrorCode() {
      return "UNKNOWN_BUCKET_NAME";
   }

   public String getErrorMessage() {
      return String.format("Bucket is not found: %s", this.bucketName);
   }

   public String toString() {
      return "UnknownBucketNameException(bucketName=" + this.bucketName + ")";
   }
}
