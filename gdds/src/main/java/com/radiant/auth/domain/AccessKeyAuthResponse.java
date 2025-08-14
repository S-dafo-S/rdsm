package com.radiant.auth.domain;

public class AccessKeyAuthResponse {
   private String token;

   public AccessKeyAuthResponse(String token) {
      this.token = token;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(final String token) {
      this.token = token;
   }

   public AccessKeyAuthResponse() {
   }

   public String toString() {
      return "AccessKeyAuthResponse(token=" + this.getToken() + ")";
   }
}
