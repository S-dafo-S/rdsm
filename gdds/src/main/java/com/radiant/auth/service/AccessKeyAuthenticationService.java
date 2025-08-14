package com.radiant.auth.service;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.auth.domain.AccessKeyAuthRequest;
import com.radiant.auth.domain.AccessKeyAuthResponse;
import com.radiant.auth.domain.ApplicationHeader;

public interface AccessKeyAuthenticationService {
   String createToken(AccessKeyAuthRequest authRequest, String ipAddress);

   String createHeader(AccessKeyAuthRequest authRequest, String ipAddress);

   AccessKeyAuthResponse createAuthResponse(AccessKeyAuthRequest authRequest, String ipAddress);

   Boolean isTokenValid(ApplicationRegistry app, String token, String ipAddress);

   Boolean isHeaderValid(ApplicationHeader header, String ipAddress);

   void updateQueryTime(ApplicationRegistry app, String token);

   void updateResponseTime(String authHeader);
}
