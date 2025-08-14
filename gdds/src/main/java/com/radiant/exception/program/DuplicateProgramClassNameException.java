package com.radiant.exception.program;

import com.radiant.exception.SystemException;

public class DuplicateProgramClassNameException extends SystemException {
   final String className;
   final String queryName;

   public DuplicateProgramClassNameException(String className, String queryName) {
      this.className = className;
      this.queryName = queryName;
   }

   public String getErrorCode() {
      return "DUPLICATE_PROGRAM_CLASS_NAME";
   }

   public String getErrorMessage() {
      return this.className;
   }

   public String toString() {
      return "DuplicateProgramClassNameException(className=" + this.className + ", queryName=" + this.queryName + ")";
   }
}
