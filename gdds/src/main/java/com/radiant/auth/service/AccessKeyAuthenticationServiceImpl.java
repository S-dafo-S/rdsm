package com.radiant.auth.service;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.repository.ApplicationRegistryRepository;
import com.radiant.auth.ApplicationUserToken;
import com.radiant.auth.AuthUtils;
import com.radiant.auth.domain.AccessKeyAuthRequest;
import com.radiant.auth.domain.AccessKeyAuthResponse;
import com.radiant.auth.domain.ApplicationHeader;
import com.radiant.auth.domain.ApplicationHeaderRepository;
import com.radiant.auth.domain.ApplicationToken;
import com.radiant.auth.domain.ApplicationTokenRepository;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessKeyAuthenticationServiceImpl implements AccessKeyAuthenticationService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(AccessKeyAuthenticationServiceImpl.class);
   private static final int APPLICATION_HEADER_LENGTH = 64;
   @Autowired
   private AccessKeyAuthenticationProvider authenticationProvider;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private ApplicationTokenRepository applicationTokenRepository;
   @Autowired
   private ApplicationHeaderRepository applicationHeaderRepository;
   @Autowired
   private ApplicationRegistryRepository applicationRegistryRepository;
   @Autowired
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;
   @Autowired
   private AccessLogService accessLogService;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   private static final Integer DEFAULT_TIMEOUT_MINUTES = 30;

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addShortPeriodActivity(this, "Cleanup expired app tokens", (Object)null);
      }

   }

   public void performActivity(Object context) {
      this.applicationRegistryRepository.findAll().forEach((app) -> {
         Date expiration = this.calculateExpiration(app);
         LOG.trace("Deleting expired tokens for app: {}", app.getAppId());
         this.applicationTokenRepository.deleteExpired(app, expiration);
      });
      LOG.trace("Deleting expired headers");
      this.applicationHeaderRepository.deleteAllByExpirationDateBefore(new Date());
   }

   @Transactional
   public String createToken(AccessKeyAuthRequest authRequest, String ipAddress) {
      ApplicationRegistry app = this.authenticate(authRequest, ipAddress);
      String userId = (String)Optional.ofNullable(authRequest.getUserId()).orElse("");
      String userName = (String)Optional.ofNullable(authRequest.getUsername()).orElse("");
      String token = this.jwtTokenService.generateToken(app.getAppId(), userName, userId, authRequest.getOrgName(), authRequest.getOrgId(), authRequest.getBusSysId());
      Date issueDate = this.jwtTokenService.getIssueDateFromToken(token);
      ApplicationToken tokenEntry = new ApplicationToken(app, token, userName, userId, issueDate);
      tokenEntry.setOrgName(authRequest.getOrgName());
      tokenEntry.setOrgId(authRequest.getOrgId());
      tokenEntry.setExternalSystemId(authRequest.getBusSysId());
      this.applicationTokenRepository.save(tokenEntry);
      SecurityContextHolder.getContext().setAuthentication((Authentication)null);
      return token;
   }

   @Transactional
   public String createHeader(AccessKeyAuthRequest authRequest, String ipAddress) {
      ApplicationRegistry app = this.authenticate(authRequest, ipAddress);
      String header = RandomStringUtils.randomAlphanumeric(64);
      ApplicationHeader headerEntry = new ApplicationHeader();
      headerEntry.setApplication(app);
      headerEntry.setOrgId(authRequest.getOrgId());
      headerEntry.setOrgName(authRequest.getOrgName());
      headerEntry.setExternalSystemId(authRequest.getBusSysId());
      headerEntry.setHeader(header);
      headerEntry.setExpirationDate(DateUtils.addMinutes(new Date(), app.getSessionLeaseTime()));
      this.applicationHeaderRepository.save(headerEntry);
      SecurityContextHolder.getContext().setAuthentication((Authentication)null);
      return header;
   }

   public AccessKeyAuthResponse createAuthResponse(AccessKeyAuthRequest authRequest, String ipAddress) {
      LOG.info("App authentication starting, request data: {}, ip: {}", authRequest, ipAddress);
      AccessKeyAuthResponse accessKeyAuthResponse = new AccessKeyAuthResponse(this.createToken(authRequest, ipAddress));
      LOG.info("App authentication response: {}", accessKeyAuthResponse);
      ExternalProgramBody fakeBody = new ExternalProgramBody();
      fakeBody.getRest().put("AccessKeyAuthRequest", authRequest.toString());
      this.accessLogService.logSuccess(accessKeyAuthResponse.toString().length(), fakeBody);
      this.serviceLogManagementService.log(LogLevel.INFO, fakeBody.toString(), HttpStatus.OK.value(), accessKeyAuthResponse.toString().length());
      return accessKeyAuthResponse;
   }

   public Boolean isTokenValid(ApplicationRegistry app, String token, String ipAddress) {
      if (!AuthUtils.isRequesterIpValid(app, ipAddress)) {
         LOG.error("Unauthorized IP address {} for app: {}", ipAddress, app.getAppId());
         return false;
      } else {
         Date now = new Date();
         if (!this.jwtTokenService.validateTokenClaims(app.getAppId(), token)) {
            return false;
         } else {
            Optional<ApplicationToken> tokenEntry = this.applicationTokenRepository.findFirstByApplicationAppIdAndToken(app.getAppId(), token);
            if (!tokenEntry.isPresent()) {
               return false;
            } else {
               Date latestTokenEvent = (Date)Stream.of(((ApplicationToken)tokenEntry.get()).getAuthenticationTime(), ((ApplicationToken)tokenEntry.get()).getLastQueryTime(), ((ApplicationToken)tokenEntry.get()).getLastResponseTime()).filter(Objects::nonNull).max(Date::compareTo).orElse(new Date());
               Date expirationDate = this.calculateExpirationDate(latestTokenEvent, app);
               return expirationDate.after(now);
            }
         }
      }
   }

   public Boolean isHeaderValid(ApplicationHeader header, String ipAddress) {
      if (!AuthUtils.isRequesterIpValid(header.getApplication(), ipAddress)) {
         LOG.error("Unauthorized IP address {} for app: {}", ipAddress, header.getApplication().getAppId());
      }

      return header.getExpirationDate().after(new Date());
   }

   public void updateQueryTime(ApplicationRegistry app, String token) {
      for(ApplicationToken applicationToken : this.applicationTokenRepository.findByApplicationAppIdAndToken(app.getAppId(), token)) {
         applicationToken.setLastQueryTime(new Date());
         this.applicationTokenRepository.saveAndFlush(applicationToken);
      }

   }

   @Transactional(
      propagation = Propagation.REQUIRES_NEW
   )
   public void updateResponseTime(String authHeader) {
      String token = this.jwtTokenService.getTokenFromHeader(authHeader);
      if (token != null) {
         String appId = this.jwtTokenService.getAppIdFromToken(token);

         for(ApplicationToken applicationToken : this.applicationTokenRepository.findByApplicationAppIdAndToken(appId, token)) {
            applicationToken.setLastResponseTime(new Date());
            this.applicationTokenRepository.saveAndFlush(applicationToken);
         }

      }
   }

   private ApplicationRegistry authenticate(AccessKeyAuthRequest authRequest, String ipAddress) {
      ApplicationRegistry app = (ApplicationRegistry)this.applicationRegistryRepository.findByAppId(authRequest.getAppId()).orElseThrow(() -> new BadCredentialsException("Bad credentials"));
      if (!AuthUtils.isRequesterIpValid(app, ipAddress)) {
         LOG.error("Unauthorized IP address {} for app: {}", ipAddress, app.getAppId());
         throw new BadCredentialsException("Bad credentials");
      } else {
         String userId = (String)Optional.ofNullable(authRequest.getUserId()).orElse("");
         String userName = (String)Optional.ofNullable(authRequest.getUsername()).orElse("");
         this.authenticationProvider.authenticate(new ApplicationUserToken(app.getAppId(), authRequest.getPassword(), userName, userId));
         return app;
      }
   }

   private Date calculateExpirationDate(Date eventTime, ApplicationRegistry app) {
      Integer configuredTimeout = app.getSessionLeaseTime();
      int timeout = configuredTimeout != null ? configuredTimeout : DEFAULT_TIMEOUT_MINUTES;
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(eventTime);
      calendar.add(12, timeout);
      return calendar.getTime();
   }

   private Date calculateExpiration(ApplicationRegistry app) {
      Integer configuredTimeout = app.getSessionLeaseTime();
      int timeout = configuredTimeout != null ? configuredTimeout : DEFAULT_TIMEOUT_MINUTES;
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(12, -timeout);
      return calendar.getTime();
   }
}
