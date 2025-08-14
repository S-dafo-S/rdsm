package com.radiant.query.domain.dto;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class QueryTestExecutionDto extends DssQueryImplDto {
   private @NotNull Long testCourt = 1L;
   private Map<String, String> testArguments = new HashMap();

   public Long getTestCourt() {
      return this.testCourt;
   }

   public Map<String, String> getTestArguments() {
      return this.testArguments;
   }

   public void setTestCourt(final Long testCourt) {
      this.testCourt = testCourt;
   }

   public void setTestArguments(final Map<String, String> testArguments) {
      this.testArguments = testArguments;
   }
}
