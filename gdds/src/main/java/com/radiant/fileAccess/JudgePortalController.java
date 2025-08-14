package com.radiant.fileAccess;

import com.radiant.account.exception.NotFoundException;
import com.radiant.exception.NotImplementedException;
import com.radiant.exception.RestNotFoundException;
import com.radiant.exception.RestServerException;
import com.radiant.exception.SystemException;
import com.radiant.exception.dataSharingSystem.DSSConnectionException;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.judgePortal.dto.JudgePortalResponse;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ParametersAreNonnullByDefault
public abstract class JudgePortalController {
   private static final Logger LOG = LoggerFactory.getLogger(JudgePortalController.class);
   @Autowired
   private I18nService i18n;
   @Autowired
   private ServiceLogManagementService serviceLog;
   @Autowired
   private AccessLogService accessLogService;

   @ExceptionHandler({AuthenticationException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(AuthenticationException ex) {
      return this.jpError(HttpStatus.UNAUTHORIZED, "authentication.failed", "AUTH_ERROR", ex);
   }

   @ExceptionHandler({RestServerException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(RestServerException ex) {
      String messageCode = "internal.error";
      if (ex.getErrorCode().equals("COURT_DATA_SYSTEM_IS_NOT_CONFIGURED")) {
         messageCode = "court.not.deployed";
      }

      return this.jpError(HttpStatus.INTERNAL_SERVER_ERROR, messageCode, ex.getErrorCode(), ex);
   }

   @ExceptionHandler({NotFoundException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(NotFoundException ex) {
      return this.jpError(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND, "not.found", ex.getErrorCode(), ex);
   }

   @ExceptionHandler({RestNotFoundException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(RestNotFoundException ex) {
      return this.jpError(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND, "not.found", ex.getErrorCode(), ex);
   }

   @ExceptionHandler({NotImplementedException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(NotImplementedException ex) {
      return this.jpError(HttpStatus.NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED, "not.implemented", ex.getErrorCode(), ex);
   }

   @ExceptionHandler({DSSConnectionException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(DSSConnectionException ex) {
      return this.jpError(HttpStatus.GATEWAY_TIMEOUT, HttpStatus.GATEWAY_TIMEOUT, "gateway.timeout", ex.getErrorCode(), ex);
   }

   @ExceptionHandler({SystemException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(SystemException ex, HttpServletResponse response) {
      HttpStatus httpStatus;
      HttpStatus jsonStatus;
      String messageCode;
      switch (ex.getErrorCode()) {
         case "COURT_IS_NOT_HOSTED":
            httpStatus = HttpStatus.OK;
            jsonStatus = HttpStatus.BAD_REQUEST;
            messageCode = "not.hosted.court";
            break;
         case "DNODE_NOT_FOUND_FOR_COURT":
            httpStatus = HttpStatus.NOT_IMPLEMENTED;
            jsonStatus = HttpStatus.NOT_IMPLEMENTED;
            messageCode = "dss.not.found.for.court";
            break;
         default:
            httpStatus = HttpStatus.BAD_REQUEST;
            jsonStatus = HttpStatus.BAD_REQUEST;
            messageCode = "bad.request";
      }

      LOG.info("Response committed {}", response.isCommitted());
      if (response.isCommitted()) {
         LOG.warn("Unexpected error in committed response");
      }

      return this.jpError(jsonStatus, httpStatus, messageCode, ex.getErrorCode(), ex);
   }

   @ExceptionHandler({RuntimeException.class})
   public ResponseEntity<JudgePortalResponse<Void>> exceptionHandler(RuntimeException ex) {
      return this.jpError(HttpStatus.INTERNAL_SERVER_ERROR, "internal.error", (String)null, ex);
   }

   private @NotNull ResponseEntity<JudgePortalResponse<Void>> jpError(HttpStatus httpStatus, String messageCode, @Nullable String errorCode, RuntimeException ex) {
      this.accessLogService.logFail(httpStatus.value(), this.i18n.message(messageCode));
      return this.jpError(httpStatus, httpStatus, messageCode, errorCode, ex);
   }

   private @NotNull ResponseEntity<JudgePortalResponse<Void>> jpError(HttpStatus jsonStatus, HttpStatus httpStatus, String messageCode, @Nullable String errorCode, RuntimeException ex) {
      LOG.error("JP request failed, error code: {}, message {}:", new Object[]{errorCode, ex.getMessage(), ex});
      String errorMessage = ex.getMessage() != null ? messageCode + ": " + ex.getMessage() : messageCode;
      this.accessLogService.logFail(httpStatus.value(), this.i18n.message(messageCode));
      this.serviceLog.log(LogLevel.ERROR, errorMessage, httpStatus.value(), 0);
      return new ResponseEntity(JudgePortalUtil.jpResponse((Object)null, jsonStatus, this.i18n.message(messageCode), errorCode), httpStatus);
   }
}
