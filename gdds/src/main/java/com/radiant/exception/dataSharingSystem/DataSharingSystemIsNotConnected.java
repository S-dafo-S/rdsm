package com.radiant.exception.dataSharingSystem;

import com.radiant.exception.SystemException;

public class DataSharingSystemIsNotConnected extends SystemException {
   private final Long dssId;

   public DataSharingSystemIsNotConnected(Long dssId) {
      this.dssId = dssId;
   }

   public String getErrorCode() {
      return "DNODE_IS_NOT_CONNECTED";
   }

   public String getErrorMessage() {
      return "DNODE ID: " + this.dssId;
   }

   public Long getDssId() {
      return this.dssId;
   }

   public String toString() {
      return "DataSharingSystemIsNotConnected(dssId=" + this.getDssId() + ")";
   }
}
