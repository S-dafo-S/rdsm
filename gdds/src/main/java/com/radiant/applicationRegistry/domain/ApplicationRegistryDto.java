package com.radiant.applicationRegistry.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationRegistryDto {
   private Long id;
   private String appId;
   private String appName;
   private String newPassword;
   private Integer sessionLeaseTime;
   private List<IpAddressDto> ipAddresses = new ArrayList();
   private List<Long> dnodeAccess = new ArrayList();
   private List<Long> apiAccess = new ArrayList();

   public ApplicationRegistryDto(ApplicationRegistry applicationRegistry) {
      this.id = applicationRegistry.getId();
      this.appId = applicationRegistry.getAppId();
      this.appName = applicationRegistry.getAppName();
      this.sessionLeaseTime = applicationRegistry.getSessionLeaseTime();
      this.ipAddresses = (List)applicationRegistry.getIpAddresses().stream().map((ad) -> new IpAddressDto(ad.getAddress())).collect(Collectors.toList());
      this.dnodeAccess = applicationRegistry.getDnodeAccess();
      this.apiAccess = applicationRegistry.getApiAccess();
   }

   public Long getId() {
      return this.id;
   }

   public String getAppId() {
      return this.appId;
   }

   public String getAppName() {
      return this.appName;
   }

   public String getNewPassword() {
      return this.newPassword;
   }

   public Integer getSessionLeaseTime() {
      return this.sessionLeaseTime;
   }

   public List<IpAddressDto> getIpAddresses() {
      return this.ipAddresses;
   }

   public List<Long> getDnodeAccess() {
      return this.dnodeAccess;
   }

   public List<Long> getApiAccess() {
      return this.apiAccess;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setAppId(final String appId) {
      this.appId = appId;
   }

   public void setAppName(final String appName) {
      this.appName = appName;
   }

   public void setNewPassword(final String newPassword) {
      this.newPassword = newPassword;
   }

   public void setSessionLeaseTime(final Integer sessionLeaseTime) {
      this.sessionLeaseTime = sessionLeaseTime;
   }

   public void setIpAddresses(final List<IpAddressDto> ipAddresses) {
      this.ipAddresses = ipAddresses;
   }

   public void setDnodeAccess(final List<Long> dnodeAccess) {
      this.dnodeAccess = dnodeAccess;
   }

   public void setApiAccess(final List<Long> apiAccess) {
      this.apiAccess = apiAccess;
   }

   public String toString() {
      return "ApplicationRegistryDto(id=" + this.getId() + ", appId=" + this.getAppId() + ", appName=" + this.getAppName() + ", newPassword=" + this.getNewPassword() + ", sessionLeaseTime=" + this.getSessionLeaseTime() + ", ipAddresses=" + this.getIpAddresses() + ", dnodeAccess=" + this.getDnodeAccess() + ", apiAccess=" + this.getApiAccess() + ")";
   }

   public ApplicationRegistryDto() {
   }
}
