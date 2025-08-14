package com.radiant.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.log.service.service.ServiceLogManagementService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class TokenAuthFilter extends OncePerRequestFilter {
   private static final Logger LOG = LoggerFactory.getLogger(TokenAuthFilter.class);
   private static final ObjectMapper MAPPER = new ObjectMapper();
   private final I18nService i18n;
   private final JwtTokenService jwtTokenService;
   private final ApplicationPropertyService propertyService;
   private final ServiceLogManagementService serviceLogManagementService;

   public TokenAuthFilter(JwtTokenService jwtTokenService, ApplicationPropertyService propertyService, ServiceLogManagementService serviceLogManagementService, I18nService i18n) {
      this.jwtTokenService = jwtTokenService;
      this.propertyService = propertyService;
      this.serviceLogManagementService = serviceLogManagementService;
      this.i18n = i18n;
   }

   protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
      if (SecurityContextHolder.getContext().getAuthentication() == null || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
         String requestHeader = request.getHeader("Authorization");
         LOG.info("API request: {} - {}", request.getRequestURI(), request.getRemoteAddr());
         String requestToken = this.jwtTokenService.getTokenFromHeader(requestHeader);
         String dssToken = this.propertyService.getStringValue("token");
         if (request.getRequestURI().startsWith("/api/v1/readfile/") && StringUtils.isEmpty(request.getHeader("X-Forwarded-For")) && "127.0.0.1".equals(request.getRemoteAddr())) {
            dssToken = null;
         }

         if (dssToken != null && !dssToken.equals(requestToken)) {
            this.serviceLogManagementService.error("AUTH_FAILED");
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(MAPPER.writeValueAsString(JudgePortalUtil.jpResponse((Object)null, HttpStatus.UNAUTHORIZED, this.i18n.message("authentication.failed"), "AUTH_ERROR")));
            return;
         }
      }

      filterChain.doFilter(request, response);
   }
}
