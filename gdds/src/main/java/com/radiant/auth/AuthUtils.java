package com.radiant.auth;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.IpAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class AuthUtils {
   public static boolean isRequesterIpValid(ApplicationRegistry app, String ipAddress) {
      if (app.getIpAddresses() != null && !app.getIpAddresses().isEmpty()) {
         // collect IP addresses allowed for the application
         List<String> allowedIps = app.getIpAddresses().stream()
            .map(IpAddress::getAddress)
            .collect(Collectors.toList());

         // ensure requester IP address is provided
         if (StringUtils.isNotBlank(ipAddress)) {
            // split provided IP addresses and validate each against allowed list
            for (String requestIp : Arrays.asList(ipAddress.replace(" ", "").split(","))) {
               if (!allowedIps.contains(requestIp)) {
                  return false; // found IP not in allowed list
               }
            }
            return true; // all requested IPs are allowed
         }

         return false; // requester IP not provided
      } else {
         return true;
      }
   }
}
