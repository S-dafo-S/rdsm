package com.radiant.query.service;

import com.radiant.plugin.GlobalVariables;
import com.radiant.query.domain.DssQuery;

public class JavaQueryArgumentResolver {
   private final DssQuery query;

   public JavaQueryArgumentResolver(DssQuery query) {
      this.query = query;
   }

   public GlobalVariables resolveGlobalVariables() {
      GlobalVariables result = new GlobalVariables();
      result.setCaseCat(this.query.getCaseType().toString().toLowerCase());
      return result;
   }

   public DssQuery getQuery() {
      return this.query;
   }
}
