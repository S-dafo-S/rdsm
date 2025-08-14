package com.radiant.config;

import com.radiant.applicationRegistry.domain.repository.ApplicationRegistryRepository;
import com.radiant.auth.AccessKeyAuthFilter;
import com.radiant.auth.domain.ApplicationHeaderRepository;
import com.radiant.auth.service.AccessKeyAuthenticationService;
import com.radiant.auth.service.AuthenticationEntryPointImpl;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.auth.service.SessionService;
import com.radiant.i18n.I18nService;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(2147483642)
@EnableWebSecurity
public class AccessKeySecurityConfig extends WebSecurityConfigurerAdapter {
   @Autowired
   private AuthenticationEntryPointImpl authenticationEntryPoint;
   @Autowired
   private I18nService i18n;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private ApplicationRegistryRepository applicationRegistryRepository;
   @Autowired
   private ApplicationHeaderRepository applicationHeaderRepository;
   @Autowired
   private AccessKeyAuthenticationService accessKeyAuthenticationService;
   @Autowired
   private AccessLogService accessLogService;
   @Autowired
   private SessionService sessionService;

   protected void configure(HttpSecurity http) throws Exception {
      ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)((HttpSecurity)((HttpSecurity)http.regexMatcher("(^(/radiant/rdsm)?/api/v1/(query|program|readfile)/.*)|^/api/internal/v1/dnode").addFilterBefore(this.accessKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf().disable()).exceptionHandling().authenticationEntryPoint(this.authenticationEntryPoint).and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()).authorizeRequests().anyRequest()).authenticated();
   }

   public AccessKeyAuthFilter accessKeyAuthFilter() {
      return new AccessKeyAuthFilter(this.i18n, this.serviceLogManagementService, this.jwtTokenService, this.applicationRegistryRepository, this.applicationHeaderRepository, this.accessKeyAuthenticationService, this.accessLogService, this.sessionService);
   }
}
