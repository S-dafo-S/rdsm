package com.radiant.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.log.service.service.ServiceLogManagementService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class GddsTokenAuthFilter extends OncePerRequestFilter {
   private static final Logger LOG = LoggerFactory.getLogger(GddsTokenAuthFilter.class);
   private static final ObjectMapper MAPPER = new ObjectMapper();
   private final I18nService i18n;
   private final JwtTokenService jwtTokenService;
   private final DnodeRepository dnodeRepository;
   private final ServiceLogManagementService serviceLogManagementService;

   public GddsTokenAuthFilter(I18nService i18n, JwtTokenService jwtTokenService, DnodeRepository dnodeRepository, ServiceLogManagementService serviceLogManagementService) {
      this.i18n = i18n;
      this.jwtTokenService = jwtTokenService;
      this.dnodeRepository = dnodeRepository;
      this.serviceLogManagementService = serviceLogManagementService;
   }

   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      if (SecurityContextHolder.getContext().getAuthentication() == null || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
         String requestHeader = request.getHeader("Authorization");
         LOG.trace("API request: {}, auth header: {}", request.getRequestURI(), requestHeader);
         String requestToken = this.jwtTokenService.getTokenFromHeader(requestHeader);
         if (requestToken != null && !this.dnodeRepository.existsByQnodeToken(requestToken)) {
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
