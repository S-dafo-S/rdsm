package com.radiant.auth.service;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.repository.ApplicationRegistryRepository;
import com.radiant.auth.ApplicationUserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccessKeyAuthenticationProvider implements AuthenticationProvider {
   @Autowired
   private ApplicationRegistryRepository applicationRegistryRepository;
   @Autowired
   private PasswordEncoder passwordEncoder;

   public Authentication authenticate(Authentication authentication) throws AuthenticationException {
      ApplicationUserToken token = (ApplicationUserToken)authentication;
      ApplicationRegistry app = (ApplicationRegistry)this.applicationRegistryRepository.findByAppId(token.getName()).orElseThrow(() -> new BadCredentialsException("Bad credentials"));
      if (!this.passwordEncoder.matches(authentication.getCredentials().toString(), app.getPassword())) {
         throw new BadCredentialsException("Bad credentials");
      } else if (token.getUsername() != null && token.getUserId() != null) {
         return token;
      } else {
         throw new BadCredentialsException("Bad credentials");
      }
   }

   public boolean supports(Class<?> authentication) {
      return authentication.equals(UsernamePasswordAuthenticationToken.class);
   }
}
