package com.radiant.exception.dataConnector;

import com.radiant.exception.SystemException;
import org.jetbrains.annotations.Nullable;

public class MinioFileReadException extends SystemException {
   final String parentMessage;

   public MinioFileReadException(Throwable cause) {
      super(cause);
      this.parentMessage = cause.getMessage();
   }

   public String getErrorCode() {
      return "FAILED_TO_READ_MINIO_FILE";
   }

   @Nullable
   public String getErrorMessage() {
      return this.parentMessage;
   }

   public String toString() {
      return "MinioFileReadException(parentMessage=" + this.parentMessage + ")";
   }
}
