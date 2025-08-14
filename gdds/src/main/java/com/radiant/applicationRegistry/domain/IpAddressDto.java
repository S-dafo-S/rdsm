package com.radiant.applicationRegistry.domain;

public class IpAddressDto {
   private String address;

   public IpAddressDto(String address) {
      this.address = address;
   }

   public String getAddress() {
      return this.address;
   }

   public void setAddress(final String address) {
      this.address = address;
   }

   public String toString() {
      return "IpAddressDto(address=" + this.getAddress() + ")";
   }

   public IpAddressDto() {
   }
}
