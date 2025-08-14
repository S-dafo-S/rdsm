package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;

public class DataSharingSystemNotFound extends SystemException {
   private Long id;
   private String name;

   public DataSharingSystemNotFound(Long id) {
      this.id = id;
   }

   public DataSharingSystemNotFound(String name) {
      this.name = name;
   }

   public String getErrorCode() {
      return "DNODE_NOT_FOUND";
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String toString() {
      return "DataSharingSystemNotFound(id=" + this.getId() + ", name=" + this.getName() + ")";
   }
}
