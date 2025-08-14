package com.radiant.dataSharingSystem.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.dataSharingSystem.domain.DNode;

public class DssDetailsDto extends DssDto {
   private String accountId;
   private String token;
   @JsonProperty(
      access = Access.WRITE_ONLY
   )
   private String qnodeToken;
   @JsonProperty(
      access = Access.WRITE_ONLY
   )
   private String accountPassword;

   public DssDetailsDto(DNode dnode) {
      super(dnode);
      this.accountId = dnode.getAccountId();
   }

   public String getAccountId() {
      return this.accountId;
   }

   public String getToken() {
      return this.token;
   }

   public String getQnodeToken() {
      return this.qnodeToken;
   }

   public String getAccountPassword() {
      return this.accountPassword;
   }

   public void setAccountId(final String accountId) {
      this.accountId = accountId;
   }

   public void setToken(final String token) {
      this.token = token;
   }

   @JsonProperty(
      access = Access.WRITE_ONLY
   )
   public void setQnodeToken(final String qnodeToken) {
      this.qnodeToken = qnodeToken;
   }

   @JsonProperty(
      access = Access.WRITE_ONLY
   )
   public void setAccountPassword(final String accountPassword) {
      this.accountPassword = accountPassword;
   }

   public DssDetailsDto() {
   }
}
