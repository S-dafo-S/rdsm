package com.radiant.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class ApplicationUserToken extends UsernamePasswordAuthenticationToken {
   private final String username;
   private final String userId;

   public ApplicationUserToken(Object principal, Object credentials, String username, String userId) {
      super(principal, credentials);
      this.username = username;
      this.userId = userId;
   }

   public String getUsername() {
      return this.username;
   }

   public String getUserId() {
      return this.userId;
   }
}
