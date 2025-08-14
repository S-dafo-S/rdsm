package com.radiant.applicationRegistry.service;

import com.radiant.account.domain.User;
import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.applicationRegistry.domain.ApplicationRegistryDto;
import com.radiant.applicationRegistry.domain.IpAddress;
import com.radiant.applicationRegistry.domain.IpAddressDto;
import com.radiant.applicationRegistry.domain.repository.ApplicationRegistryRepository;
import com.radiant.auth.domain.ApplicationHeaderRepository;
import com.radiant.auth.domain.ApplicationTokenRepository;
import com.radiant.auth.service.CurrentUser;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.exception.applicationRegistry.DuplicateAppId;
import com.radiant.exception.applicationRegistry.InvalidIpAddressFormat;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.query.domain.GddsQuery;
import com.radiant.query.domain.GddsQueryRepository;
import com.radiant.util.DBUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationRegistryServiceImpl implements ApplicationRegistryService {
   @Autowired
   private ApplicationRegistryRepository repository;
   @Autowired
   private ApplicationTokenRepository applicationTokenRepository;
   @Autowired
   private ApplicationHeaderRepository applicationHeaderRepository;
   @Autowired
   private PasswordEncoder passwordEncoder;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private GddsQueryRepository gddsQueryRepository;

   public ApplicationRegistryDto get(Long id) {
      return new ApplicationRegistryDto((ApplicationRegistry)this.repository.getById(id));
   }

   public List<ApplicationRegistryDto> getAll() {
      return (List)this.repository.findAll().stream().map(ApplicationRegistryDto::new).collect(Collectors.toList());
   }

   public ApplicationRegistryDto create(ApplicationRegistryDto request) {
      ApplicationRegistry appRegistry = new ApplicationRegistry();
      this.saveFromRequest(appRegistry, request);
      this.auditLogService.created((User)this.currentUser.get(), appRegistry).logMessage("Application registry {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
      return new ApplicationRegistryDto(appRegistry);
   }

   public ApplicationRegistryDto update(Long id, ApplicationRegistryDto request) {
      ApplicationRegistry appRegistry = (ApplicationRegistry)this.repository.getById(id);
      this.saveFromRequest(appRegistry, request);
      this.auditLogService.updated((User)this.currentUser.get(), appRegistry).logMessage("Application registry {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new ApplicationRegistryDto(appRegistry);
   }

   public void delete(Long id) {
      this.repository.findById(id).ifPresent((applicationRegistry) -> {
         AuditObject logObject = applicationRegistry.toAuditObject();
         this.applicationTokenRepository.deleteAllByApplication(applicationRegistry);
         this.applicationHeaderRepository.deleteAllByApplication(applicationRegistry);
         this.repository.delete(applicationRegistry);
         this.auditLogService.deleted((User)this.currentUser.get(), logObject).logMessage("Application registry {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});
      });
   }

   public void validateApiAccess(HttpServletRequest request, String apiName) {
      Optional<ApplicationRegistry> optionalApplicationRegistry = this.getApplicationRegistry(request);
      optionalApplicationRegistry.ifPresent((applicationRegistry) -> {
         List<Long> allowedApi = applicationRegistry.getApiAccess();
         if (!allowedApi.isEmpty()) {
            Optional<GddsQuery> query = this.gddsQueryRepository.findByNameIgnoreCase(apiName);
            if (!query.isPresent() || allowedApi.stream().noneMatch((apiId) -> ((GddsQuery)query.get()).getId().equals(apiId))) {
               throw new BadCredentialsException("Unauthorized API access");
            }
         }
      });
   }

   public void validateDssAccess(HttpServletRequest request, Set<Long> dssIds) {
      if (!dssIds.isEmpty()) {
         Optional<ApplicationRegistry> optionalApplicationRegistry = this.getApplicationRegistry(request);
         optionalApplicationRegistry.ifPresent((applicationRegistry) -> {
            List<Long> allowedDss = applicationRegistry.getDnodeAccess();
            if (!allowedDss.isEmpty()) {
               if (dssIds.stream().anyMatch((dssId) -> !allowedDss.contains(dssId))) {
                  throw new BadCredentialsException("Unauthorized DSS access");
               }
            }
         });
      }
   }

   private ApplicationRegistry saveFromRequest(ApplicationRegistry appRegistry, ApplicationRegistryDto request) {
      this.validateIpAddressFormatting(request.getIpAddresses());
      appRegistry.setAppId(request.getAppId());
      appRegistry.setAppName(request.getAppName());
      appRegistry.setSessionLeaseTime(request.getSessionLeaseTime());
      appRegistry.getIpAddresses().clear();

      for(IpAddressDto dto : request.getIpAddresses()) {
         appRegistry.getIpAddresses().add(new IpAddress(dto.getAddress(), appRegistry));
      }

      appRegistry.getDnodeAccess().clear();
      appRegistry.getDnodeAccess().addAll(request.getDnodeAccess());
      appRegistry.getApiAccess().clear();
      appRegistry.getApiAccess().addAll(request.getApiAccess());
      UpdateEntityHelper.ifNotNull(request.getNewPassword(), (password) -> appRegistry.setPassword(this.passwordEncoder.encode(password)));

      try {
         return (ApplicationRegistry)this.repository.saveAndFlush(appRegistry);
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "application_registry_app_id_unic")) {
            throw new DuplicateAppId(request.getAppId());
         } else {
            throw e;
         }
      }
   }

   private Optional<ApplicationRegistry> getApplicationRegistry(HttpServletRequest request) {
      String authHeader = request.getHeader("Authorization");
      String apiGatewayToken = request.getHeader("token");
      String authToken = !Strings.isEmpty(authHeader) ? this.jwtTokenService.getTokenFromHeader(authHeader) : this.jwtTokenService.getTokenFromHeader(apiGatewayToken);
      if (StringUtils.isNotEmpty(authToken)) {
         String appId = this.jwtTokenService.getAppIdFromToken(authToken);
         return this.repository.findByAppId(appId);
      } else {
         return Optional.empty();
      }
   }

   private void validateIpAddressFormatting(List<IpAddressDto> ipAddresses) {
      Pattern ipPattern = Pattern.compile("(\\d+\\.?)*\\d+");
      if (ipAddresses != null) {
         for(IpAddressDto ipEntry : ipAddresses) {
            if (!ipPattern.matcher(ipEntry.getAddress()).matches()) {
               throw new InvalidIpAddressFormat(ipEntry.getAddress());
            }
         }
      }

   }
}
