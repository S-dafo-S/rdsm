package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;

public class DuplicateDataSharingSystemName extends SystemException {
   private String name;

   public DuplicateDataSharingSystemName(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_DNODE_NAME";
   }

   public String getName() {
      return this.name;
   }
}
