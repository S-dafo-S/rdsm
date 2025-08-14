package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;

public class DuplicateDataSharingSystemAccountId extends SystemException {
   private String name;

   public DuplicateDataSharingSystemAccountId(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DUPLICATE_DNODE_ACCOUNT_ID";
   }

   public String getName() {
      return this.name;
   }
}
