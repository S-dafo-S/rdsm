package com.radiant.log.access.service;

import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.exception.RdsmIOException;
import com.radiant.log.access.domain.AccessLog;
import com.radiant.log.access.domain.AccessLogRepository;
import com.radiant.log.access.domain.AccessLogSpecs;
import com.radiant.log.access.domain.dto.AccessLogDto;
import com.radiant.log.access.domain.dto.ExternalAccessLogDto;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.restapi.RestApiUtils;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class AccessLogServiceImpl implements AccessLogService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(AccessLogServiceImpl.class);
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private AccessLogRepository accessLogRepository;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired(
      required = false
   )
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;
   @Autowired
   @Qualifier("defaultEntityManagerFactory")
   private EntityManager entityManager;

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addShortPeriodActivity(this, "Remove outdated access logs", (Object)null);
      }

   }

   public void performActivity(Object context) {
      LOG.trace("Deleting outdated access logs");
      int retention = this.applicationPropertyService.getStringValue("access_log_retention") != null ? Integer.parseInt(this.applicationPropertyService.getStringValue("access_log_retention")) : 30;
      Date retentionDate = DateUtils.addDays(new Date(), -retention);
      this.accessLogRepository.deleteOutdatedInBulk(retentionDate);
      this.accessLogRepository.flush();
   }

   public void logSuccess(Integer responseLength, @Nullable ExternalProgramBody externalProgramBody) {
      this.constructLog(HttpStatus.OK.value(), responseLength, externalProgramBody);
   }

   public void logFail(Integer responseCode, String errorMessage) {
      AccessLog log = this.constructLog(responseCode, 0, (ExternalProgramBody)null);
      if (log != null) {
         log.setErrorMessage(errorMessage);
         this.accessLogRepository.save(log);
      }

   }

   public Page<AccessLogDto> getLogs(@Nullable Long startDate, @Nullable Long endDate, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration, Pageable pageable) {
      LOG.debug("Start retrieving access log page...");
      Page<AccessLog> logs = this.logPage().start(startDate).end(endDate).appId(appId).sysId(sysId).clientIp(clientIp).username(username).userId(userId).path(path).response(response).duration(duration).pageable(pageable).get();
      LOG.debug("Retrieving access log page finished");
      return new PageImpl((List)logs.get().map(AccessLogDto::new).collect(Collectors.toList()), pageable, logs.getTotalElements());
   }

   private Stream<AccessLog> getFilteredLogs(@Nullable Long start, @Nullable Long end, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration) {
      Specification<AccessLog> spec = this.spec().start(start).end(end).appId(appId).sysId(sysId).clientIp(clientIp).username(username).userId(userId).path(path).response(response).duration(duration).get();
      return this.accessLogRepository.findAll(spec, Sort.by(new String[]{"Id"})).stream();
   }

   @Transactional(
      readOnly = true
   )
   public void downloadLogs(OutputStream outputStream, @Nullable Long startDate, @Nullable Long endDate, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration) {
      LOG.info("Start retrieving access logs...");

        try (Writer out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(out, Builder.create(CSVFormat.DEFAULT)
                 .setHeader(new String[]{"app_id", "b_sys_id", "service_ip", "service_port", "service_path", "client_ip", "user_name", "user_id", "sc_request_id", "query_body", "query_params", "read_file_name", "error_message", "start_time", "end_time", "duration", "response_code", "response_length"}).build());
             Stream<AccessLog> logs = this.accessLogRepository.streamWithFilter(startDate != null ? new Date(startDate) : null, endDate != null ? new Date(endDate) : null, appId, sysId, clientIp, username, userId, path, response, duration != null ? (long)duration : null)) {
           SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

           logs.forEach((log) -> {
              try {
                 printer.printRecord(new Object[]{log.getAppId(), log.getSysId(), log.getServerAddress(), log.getServerPort(), log.getApiPath(), log.getClientAddress(), log.getUserName(), log.getUserId(), log.getScRequestId(), log.getQueryBody(), log.getQueryParams(), log.getReadFileName(), log.getErrorMessage(), dateFormat.format(log.getStartTime()), dateFormat.format(log.getEndTime()), log.getEndTime().getTime() - log.getStartTime().getTime(), log.getResponseCode(), log.getResponseLength()});
                 this.entityManager.detach(log);
              } catch (IOException e) {
                 throw new RdsmIOException(e);
              }
           });
           LOG.info("Access log retrieving successfully finished");
        } catch (IOException e) {
           throw new RdsmIOException(e);
        }
   }

   public List<ExternalAccessLogDto> getFilteredLogs(String start, @Nullable String end) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm");

        try {
           return (List)this.filteredLogs().start(start != null ? dateFormat.parse(start).getTime() : null).end(end != null ? dateFormat.parse(end).getTime() : null).get().map(ExternalAccessLogDto::new).collect(Collectors.toList());
        } catch (ParseException e) {
           throw new IllegalArgumentException("Failed to parse date interval");
        }
   }

   private AccessLog constructLog(Integer responseCode, Integer responseLength, @Nullable ExternalProgramBody externalProgramBody) {
      try {
         RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
         if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)attributes).getRequest();
            AccessLog log = new AccessLog();
            String requestUri = StringUtils.isNotEmpty(request.getRequestURI()) ? URLDecoder.decode(request.getRequestURI(), "UTF-8") : "";
            log.setServerAddress(request.getServerName());
            log.setServerPort(request.getServerPort());
            log.setApiPath(requestUri);
            log.setClientAddress(RestApiUtils.getRequesterIp(request));
            if (log.getClientAddress() == null) {
               log.setClientAddress(request.getRemoteAddr());
            }

            String authHeader = request.getHeader("Authorization");
            String apiGatewayHeader = request.getHeader("X-KSP-Request-Id");
            String apiGatewayToken = request.getHeader("token");
            String authToken = !Strings.isEmpty(authHeader) ? this.jwtTokenService.getTokenFromHeader(authHeader) : this.jwtTokenService.getTokenFromHeader(apiGatewayToken);
            if (authToken != null) {
               try {
                  log.setOrgId(this.jwtTokenService.getExternalOrgIdFromToken(authToken));
                  log.setOrgName(this.jwtTokenService.getExternalOrgNameFromToken(authToken));
                  log.setUserId(this.jwtTokenService.getExternalUserIdFromToken(authToken));
                  log.setUserName(this.jwtTokenService.getExternalUsernameFromToken(authToken));
                  log.setSysId(this.jwtTokenService.getExternalSystemIdFromToken(authToken));
                  log.setAppId(this.jwtTokenService.getAppIdFromToken(authToken));
                 } catch (SignatureException | MalformedJwtException e) {
                    LOG.warn("Invalid auth token, access log remains unsigned");
                 }
            }

            if (externalProgramBody != null) {
               log.setQueryBody(externalProgramBody.toString());
            }

            if (StringUtils.isNotEmpty(request.getQueryString())) {
               log.setQueryParams(URLDecoder.decode(request.getQueryString(), "UTF-8"));
            }

            if (StringUtils.isNotEmpty(log.getApiPath()) && log.getApiPath().contains("readfile")) {
               String[] pathSplited = log.getApiPath().split("/");
               if (pathSplited.length > 0) {
                  log.setReadFileName(pathSplited[pathSplited.length - 1]);
               }
            }

            if (apiGatewayHeader != null) {
               log.setScRequestId(apiGatewayHeader);
            }

            Object startTimeObj = attributes.getAttribute("startTime", 0);
            if (startTimeObj == null) {
               log.setStartTime(new Date());
            } else {
               Date start = new Date((Long)startTimeObj);
               log.setStartTime(start);
            }

            log.setEndTime(new Date());
            log.setDuration(log.getEndTime().getTime() - log.getStartTime().getTime());
            log.setResponseCode(responseCode);
            log.setResponseLength(responseLength);
            return (AccessLog)this.accessLogRepository.save(log);
         }

         LOG.warn("Unexpected request, skip access logging");
      } catch (Exception e) {
         LOG.error("Failed to log access", e);
      }

      return null;
   }

   private Specification<AccessLog> getSpecification(@Nullable Long start, @Nullable Long end, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration) {
      Specification<AccessLog> spec = Specification.where((Specification)null);
      if (start != null) {
         spec = spec.and(AccessLogSpecs.isDateAfter(new Date(start)));
      }

      if (end != null) {
         spec = spec.and(AccessLogSpecs.isDateBefore(new Date(end)));
      }

      if (appId != null) {
         spec = spec.and(AccessLogSpecs.appIdEq(appId));
      }

      if (sysId != null) {
         spec = spec.and(AccessLogSpecs.sysIdEq(sysId));
      }

      if (clientIp != null) {
         spec = spec.and(AccessLogSpecs.ipEq(clientIp));
      }

      if (username != null) {
         spec = spec.and(AccessLogSpecs.usernameContains(username));
      }

      if (userId != null) {
         spec = spec.and(AccessLogSpecs.userIdContains(userId));
      }

      if (path != null) {
         spec = spec.and(AccessLogSpecs.pathContains(path));
      }

      if (response != null) {
         spec = spec.and(AccessLogSpecs.responseIs(response));
      }

      if (duration != null) {
         spec = spec.and(AccessLogSpecs.durationMore(duration));
      }

      return spec;
   }

   private Page<AccessLog> getLogPage(@Nullable Long start, @Nullable Long end, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration, Pageable pageable) {
      Specification<AccessLog> spec = this.spec().start(start).end(end).appId(appId).sysId(sysId).clientIp(clientIp).username(username).userId(userId).path(path).response(response).duration(duration).get();
      return this.accessLogRepository.findAll(spec, pageable);
   }

   public StreamBuilder filteredLogs() {
      return new StreamBuilder();
   }

   public SpecificationBuilder spec() {
      return new SpecificationBuilder();
   }

   public PageBuilder logPage() {
      return new PageBuilder();
   }

   public class StreamBuilder {
      private Long start;
      private Long end;
      private String appId;
      private String sysId;
      private String clientIp;
      private String username;
      private String userId;
      private String path;
      private Integer response;
      private Integer duration;

      StreamBuilder() {
      }

      public StreamBuilder start(@Nullable final Long start) {
         this.start = start;
         return this;
      }

      public StreamBuilder end(@Nullable final Long end) {
         this.end = end;
         return this;
      }

      public StreamBuilder appId(@Nullable final String appId) {
         this.appId = appId;
         return this;
      }

      public StreamBuilder sysId(@Nullable final String sysId) {
         this.sysId = sysId;
         return this;
      }

      public StreamBuilder clientIp(@Nullable final String clientIp) {
         this.clientIp = clientIp;
         return this;
      }

      public StreamBuilder username(@Nullable final String username) {
         this.username = username;
         return this;
      }

      public StreamBuilder userId(@Nullable final String userId) {
         this.userId = userId;
         return this;
      }

      public StreamBuilder path(@Nullable final String path) {
         this.path = path;
         return this;
      }

      public StreamBuilder response(@Nullable final Integer response) {
         this.response = response;
         return this;
      }

      public StreamBuilder duration(@Nullable final Integer duration) {
         this.duration = duration;
         return this;
      }

      public Stream<AccessLog> get() {
         return AccessLogServiceImpl.this.getFilteredLogs(this.start, this.end, this.appId, this.sysId, this.clientIp, this.username, this.userId, this.path, this.response, this.duration);
      }

      public String toString() {
         return "AccessLogServiceImpl.StreamBuilder(start=" + this.start + ", end=" + this.end + ", appId=" + this.appId + ", sysId=" + this.sysId + ", clientIp=" + this.clientIp + ", username=" + this.username + ", userId=" + this.userId + ", path=" + this.path + ", response=" + this.response + ", duration=" + this.duration + ")";
      }
   }

   public class SpecificationBuilder {
      private Long start;
      private Long end;
      private String appId;
      private String sysId;
      private String clientIp;
      private String username;
      private String userId;
      private String path;
      private Integer response;
      private Integer duration;

      SpecificationBuilder() {
      }

      public SpecificationBuilder start(@Nullable final Long start) {
         this.start = start;
         return this;
      }

      public SpecificationBuilder end(@Nullable final Long end) {
         this.end = end;
         return this;
      }

      public SpecificationBuilder appId(@Nullable final String appId) {
         this.appId = appId;
         return this;
      }

      public SpecificationBuilder sysId(@Nullable final String sysId) {
         this.sysId = sysId;
         return this;
      }

      public SpecificationBuilder clientIp(@Nullable final String clientIp) {
         this.clientIp = clientIp;
         return this;
      }

      public SpecificationBuilder username(@Nullable final String username) {
         this.username = username;
         return this;
      }

      public SpecificationBuilder userId(@Nullable final String userId) {
         this.userId = userId;
         return this;
      }

      public SpecificationBuilder path(@Nullable final String path) {
         this.path = path;
         return this;
      }

      public SpecificationBuilder response(@Nullable final Integer response) {
         this.response = response;
         return this;
      }

      public SpecificationBuilder duration(@Nullable final Integer duration) {
         this.duration = duration;
         return this;
      }

      public Specification<AccessLog> get() {
         return AccessLogServiceImpl.this.getSpecification(this.start, this.end, this.appId, this.sysId, this.clientIp, this.username, this.userId, this.path, this.response, this.duration);
      }

      public String toString() {
         return "AccessLogServiceImpl.SpecificationBuilder(start=" + this.start + ", end=" + this.end + ", appId=" + this.appId + ", sysId=" + this.sysId + ", clientIp=" + this.clientIp + ", username=" + this.username + ", userId=" + this.userId + ", path=" + this.path + ", response=" + this.response + ", duration=" + this.duration + ")";
      }
   }

   public class PageBuilder {
      private Long start;
      private Long end;
      private String appId;
      private String sysId;
      private String clientIp;
      private String username;
      private String userId;
      private String path;
      private Integer response;
      private Integer duration;
      private Pageable pageable;

      PageBuilder() {
      }

      public PageBuilder start(@Nullable final Long start) {
         this.start = start;
         return this;
      }

      public PageBuilder end(@Nullable final Long end) {
         this.end = end;
         return this;
      }

      public PageBuilder appId(@Nullable final String appId) {
         this.appId = appId;
         return this;
      }

      public PageBuilder sysId(@Nullable final String sysId) {
         this.sysId = sysId;
         return this;
      }

      public PageBuilder clientIp(@Nullable final String clientIp) {
         this.clientIp = clientIp;
         return this;
      }

      public PageBuilder username(@Nullable final String username) {
         this.username = username;
         return this;
      }

      public PageBuilder userId(@Nullable final String userId) {
         this.userId = userId;
         return this;
      }

      public PageBuilder path(@Nullable final String path) {
         this.path = path;
         return this;
      }

      public PageBuilder response(@Nullable final Integer response) {
         this.response = response;
         return this;
      }

      public PageBuilder duration(@Nullable final Integer duration) {
         this.duration = duration;
         return this;
      }

      public PageBuilder pageable(final Pageable pageable) {
         this.pageable = pageable;
         return this;
      }

      public Page<AccessLog> get() {
         return AccessLogServiceImpl.this.getLogPage(this.start, this.end, this.appId, this.sysId, this.clientIp, this.username, this.userId, this.path, this.response, this.duration, this.pageable);
      }

      public String toString() {
         return "AccessLogServiceImpl.PageBuilder(start=" + this.start + ", end=" + this.end + ", appId=" + this.appId + ", sysId=" + this.sysId + ", clientIp=" + this.clientIp + ", username=" + this.username + ", userId=" + this.userId + ", path=" + this.path + ", response=" + this.response + ", duration=" + this.duration + ", pageable=" + this.pageable + ")";
      }
   }
}
