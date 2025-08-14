package com.radiant.auth.domain;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;

public class AccessKeyAuthRequest {
   @ApiModelProperty("Application id")
   private @NotEmpty(
   message = "Application ID must not be empty"
) String appId;
   @ApiModelProperty("Application password")
   private @NotEmpty(
   message = "Application password must not be empty"
) String password;
   @ApiModelProperty("External user name")
   private String username;
   @ApiModelProperty("External user id")
   private String userId;
   @ApiModelProperty("External organization name, optional")
   private String orgName;
   @ApiModelProperty("External organization id, optional")
   private String orgId;
   @ApiModelProperty("External system id, optional")
   private String busSysId;

   public String getAppId() {
      return this.appId;
   }

   public String getPassword() {
      return this.password;
   }

   public String getUsername() {
      return this.username;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getOrgName() {
      return this.orgName;
   }

   public String getOrgId() {
      return this.orgId;
   }

   public String getBusSysId() {
      return this.busSysId;
   }

   public void setAppId(final String appId) {
      this.appId = appId;
   }

   public void setPassword(final String password) {
      this.password = password;
   }

   public void setUsername(final String username) {
      this.username = username;
   }

   public void setUserId(final String userId) {
      this.userId = userId;
   }

   public void setOrgName(final String orgName) {
      this.orgName = orgName;
   }

   public void setOrgId(final String orgId) {
      this.orgId = orgId;
   }

   public void setBusSysId(final String busSysId) {
      this.busSysId = busSysId;
   }

   public String toString() {
      return "AccessKeyAuthRequest(appId=" + this.getAppId() + ", username=" + this.getUsername() + ", userId=" + this.getUserId() + ", orgName=" + this.getOrgName() + ", orgId=" + this.getOrgId() + ", busSysId=" + this.getBusSysId() + ")";
   }
}
