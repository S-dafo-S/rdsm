package com.radiant.config;

import com.radiant.auth.GddsTokenAuthFilter;
import com.radiant.auth.service.AuthenticationEntryPointImpl;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
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
@Order(2147483645)
@EnableWebSecurity
public class GddsTokenSecurityConfig extends WebSecurityConfigurerAdapter {
   @Autowired
   private AuthenticationEntryPointImpl authenticationEntryPoint;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private DnodeRepository dnodeRepository;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Autowired
   private I18nService i18n;

   protected void configure(HttpSecurity http) throws Exception {
      ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)((HttpSecurity)((HttpSecurity)http.regexMatcher("^/api/internal/v1/(settings|court|region)/.*").addFilterBefore(this.gddsTokenAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf().disable()).exceptionHandling().authenticationEntryPoint(this.authenticationEntryPoint).and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()).authorizeRequests().anyRequest()).anonymous();
   }

   public GddsTokenAuthFilter gddsTokenAuthFilter() {
      return new GddsTokenAuthFilter(this.i18n, this.jwtTokenService, this.dnodeRepository, this.serviceLogManagementService);
   }
}
