package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;

public class DataSharingSystemNotEditable extends SystemException {
   private Long id;

   public DataSharingSystemNotEditable(Long id) {
      this.id = id;
   }

   public String getErrorCode() {
      return "DNODE_NOT_EDITABLE";
   }

   public Long getId() {
      return this.id;
   }

   public String toString() {
      return "DataSharingSystemNotEditable(id=" + this.getId() + ")";
   }
}
