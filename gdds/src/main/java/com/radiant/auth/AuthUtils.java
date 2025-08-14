package com.radiant.auth;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.IpAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class AuthUtils {
   public static Boolean isRequesterIpValid(ApplicationRegistry app, String ipAddress) {
      if (app.getIpAddresses() != null && !app.getIpAddresses().isEmpty()) {
         return StringUtils.isEmpty(ipAddress) ? false : ((List)app.getIpAddresses().stream().map(IpAddress::getAddress).collect(Collectors.toList())).containsAll(Arrays.asList(ipAddress.replace(" ", "").split(",")));
      } else {
         return true;
      }
   }
}
