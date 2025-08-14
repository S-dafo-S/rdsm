package com.radiant.exception.query;

import com.radiant.account.exception.NotFoundException;
import javax.annotation.Nullable;

public class JavaQueryImplProgramNotFound extends NotFoundException {
   private String queryName;

   public JavaQueryImplProgramNotFound(@Nullable String name, String queryName) {
      super(name);
      this.queryName = queryName;
   }

   public String getErrorCode() {
      return "JAVA_QUERY_PROGRAM_IS_NOT_FOUND";
   }

   public String getQueryName() {
      return this.queryName;
   }

   public String toString() {
      return "JavaQueryImplProgramNotFound(queryName=" + this.getQueryName() + ")";
   }
}
