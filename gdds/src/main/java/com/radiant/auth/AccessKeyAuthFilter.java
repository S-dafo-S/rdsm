package com.radiant.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.repository.ApplicationRegistryRepository;
import com.radiant.auth.domain.AppUserPrincipal;
import com.radiant.auth.domain.ApplicationHeaderRepository;
import com.radiant.auth.service.AccessKeyAuthenticationService;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.auth.service.SessionService;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.restapi.RestApiUtils;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class AccessKeyAuthFilter extends OncePerRequestFilter {
   private static final Logger LOG = LoggerFactory.getLogger(AccessKeyAuthFilter.class);
   private static final ObjectMapper MAPPER = new ObjectMapper();
   private static final String FAILED_CODE = "authentication.failed";
   private final I18nService i18n;
   private final ServiceLogManagementService serviceLogManagementService;
   private final JwtTokenService jwtTokenService;
   private final ApplicationRegistryRepository applicationRegistryRepository;
   private final ApplicationHeaderRepository applicationHeaderRepository;
   private final AccessKeyAuthenticationService accessKeyAuthenticationService;
   private final AccessLogService accessLogService;
   private final SessionService sessionService;

   public AccessKeyAuthFilter(I18nService i18n, ServiceLogManagementService serviceLogManagementService, JwtTokenService jwtTokenService, ApplicationRegistryRepository applicationRegistryRepository, ApplicationHeaderRepository applicationHeaderRepository, AccessKeyAuthenticationService accessKeyAuthenticationService, AccessLogService accessLogService, SessionService sessionService) {
      this.i18n = i18n;
      this.serviceLogManagementService = serviceLogManagementService;
      this.jwtTokenService = jwtTokenService;
      this.applicationRegistryRepository = applicationRegistryRepository;
      this.applicationHeaderRepository = applicationHeaderRepository;
      this.accessKeyAuthenticationService = accessKeyAuthenticationService;
      this.accessLogService = accessLogService;
      this.sessionService = sessionService;
   }

   private static void setAuthFailed(HttpServletResponse response, String message, ServiceLogManagementService serviceLogManagementService, AccessLogService accessLogService) throws IOException {
      serviceLogManagementService.error("AUTH_FAILED");
      accessLogService.logFail(401, message);
      response.setStatus(401);
      response.setContentType("application/json");
      response.getWriter().write(MAPPER.writeValueAsString(JudgePortalUtil.jpResponse((Object)null, HttpStatus.UNAUTHORIZED, message, "AUTH_ERROR")));
   }

   protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
      String authHeader = request.getHeader("Authorization");
      String apiGatewayHeader = request.getHeader("X-KSP-Request-Id");
      String apiGatewayToken = request.getHeader("token");
      if (authHeader == null && apiGatewayHeader == null) {
         this.sessionService.setAuthentication(request);
         filterChain.doFilter(request, response);
      } else if ((Strings.isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) && Strings.isEmpty(apiGatewayHeader)) {
         setAuthFailed(response, this.i18n.message("authentication.failed"), this.serviceLogManagementService, this.accessLogService);
      } else {
         LOG.trace("Public API request: {}, auth header: {}, API Gateway header: {}", new Object[]{request.getRequestURI(), authHeader, apiGatewayHeader});
         String authToken = !Strings.isEmpty(authHeader) ? this.jwtTokenService.getTokenFromHeader(authHeader) : this.jwtTokenService.getTokenFromHeader(apiGatewayToken);
         String ipAddress = RestApiUtils.getRequesterIp(request);
         String appId = null;
         if (authToken != null) {
            try {
               appId = this.jwtTokenService.getAppIdFromToken(authToken);
            } catch (IllegalArgumentException e) {
               LOG.error("An error occurred during getting app ID from token", e);
            } catch (JwtException e) {
               LOG.warn("JWT error getting app ID from token", e);
            }
         }

         if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (appId == null) {
               setAuthFailed(response, this.i18n.message("authentication.failed"), this.serviceLogManagementService, this.accessLogService);
               return;
            }

            Optional<ApplicationRegistry> optionalApp = this.applicationRegistryRepository.findByAppId(appId);
            if (!optionalApp.isPresent()) {
               setAuthFailed(response, this.i18n.message("authentication.failed"), this.serviceLogManagementService, this.accessLogService);
               return;
            }

            if (!this.accessKeyAuthenticationService.isTokenValid((ApplicationRegistry)optionalApp.get(), authToken, ipAddress)) {
               setAuthFailed(response, this.i18n.message("authentication.failed"), this.serviceLogManagementService, this.accessLogService);
               return;
            }

            this.accessKeyAuthenticationService.updateQueryTime((ApplicationRegistry)optionalApp.get(), authToken);
            AppUserPrincipal userDetails = new AppUserPrincipal(((ApplicationRegistry)optionalApp.get()).getAppId(), ((ApplicationRegistry)optionalApp.get()).getPassword(), new String[]{"JP_APP"});
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, (Object)null, userDetails.getAuthorities());
            authentication.setDetails((new WebAuthenticationDetailsSource()).buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
         }

         filterChain.doFilter(request, response);
      }
   }
}
