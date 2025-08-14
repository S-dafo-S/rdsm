package com.radiant.config;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.TokenAuthFilter;
import com.radiant.auth.service.AuthenticationEntryPointImpl;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.i18n.I18nService;
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
@Order(2147483643)
@EnableWebSecurity
public class TokenSecurityConfig extends WebSecurityConfigurerAdapter {
   @Autowired
   private AuthenticationEntryPointImpl authenticationEntryPoint;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private ApplicationPropertyService propertyService;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Autowired
   private I18nService i18n;

   protected void configure(HttpSecurity http) throws Exception {
      ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)((HttpSecurity)((HttpSecurity)http.regexMatcher("^/api/v1/(settings|readfile|query|program|court)/.*").addFilterBefore(this.tokenAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf().disable()).exceptionHandling().authenticationEntryPoint(this.authenticationEntryPoint).and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()).authorizeRequests().anyRequest()).anonymous();
   }

   public TokenAuthFilter tokenAuthFilter() {
      return new TokenAuthFilter(this.jwtTokenService, this.propertyService, this.serviceLogManagementService, this.i18n);
   }
}
