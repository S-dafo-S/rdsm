package com.radiant.query.domain.dto;

import com.radiant.query.domain.GddsQuery;

public class GddsQueryDto extends QueryDto {
   public GddsQueryDto(GddsQuery query) {
      super(query, query.getArguments());
   }

   public GddsQueryDto() {
   }
}
