package com.radiant.query.domain.dto;

import com.radiant.query.domain.DssQuery;

public class DssQueryDto extends QueryDto {
   private Long implementationCount;

   public DssQueryDto(DssQuery query, Long implementationCount) {
      super(query, query.getArguments());
      this.implementationCount = implementationCount;
   }

   public Long getImplementationCount() {
      return this.implementationCount;
   }

   public void setImplementationCount(final Long implementationCount) {
      this.implementationCount = implementationCount;
   }

   public DssQueryDto() {
   }
}
