package com.radiant.exception.applicationRegistry;

import com.radiant.exception.SystemException;

public class InvalidIpAddressFormat extends SystemException {
   private final String ipAddress;

   public InvalidIpAddressFormat(String ipAddress) {
      this.ipAddress = ipAddress;
   }

   public String getErrorCode() {
      return "INVALID_IP_ADDRESS_FORMAT";
   }

   public String toString() {
      return "InvalidIpAddressFormat(ipAddress=" + this.ipAddress + ")";
   }
}
