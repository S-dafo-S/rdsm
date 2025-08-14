package com.radiant.exception.integrationFunction;

import com.radiant.exception.SystemException;

public class IntegrationFunctionProgramNotFound extends SystemException {
   final String originalFilename;
   final String uploadedFilename;

   public IntegrationFunctionProgramNotFound(String originalFilename, String uploadedFilename) {
      this.originalFilename = originalFilename;
      this.uploadedFilename = uploadedFilename;
   }

   public String getErrorCode() {
      return "INTEGRATION_FUNCTION_PROGRAM_IS_NOT_FOUND";
   }

   public String getOriginalFilename() {
      return this.originalFilename;
   }

   public String getUploadedFilename() {
      return this.uploadedFilename;
   }

   public String toString() {
      return "IntegrationFunctionProgramNotFound(originalFilename=" + this.getOriginalFilename() + ", uploadedFilename=" + this.getUploadedFilename() + ")";
   }
}
