package com.radiant.exception.program;

import com.radiant.exception.SystemException;

public class ProgramServiceNotFound extends SystemException {
   final String expectedGddsServiceName;
   final String expectedDssServiceName;

   public ProgramServiceNotFound(String expectedGddsServiceName, String expectedDssServiceName) {
      this.expectedGddsServiceName = expectedGddsServiceName;
      this.expectedDssServiceName = expectedDssServiceName;
   }

   public String getErrorCode() {
      return "EXPECTED_SERVICE_NOT_FOUND_IN_PROGRAM_JAR";
   }

   public String getErrorMessage() {
      return this.expectedGddsServiceName + "/" + this.expectedDssServiceName;
   }

   public String toString() {
      return "ProgramServiceNotFound(expectedGddsServiceName=" + this.expectedGddsServiceName + ", expectedDssServiceName=" + this.expectedDssServiceName + ")";
   }
}
