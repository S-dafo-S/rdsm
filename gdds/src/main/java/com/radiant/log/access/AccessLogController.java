package com.radiant.log.access;

import com.radiant.log.access.domain.dto.AccessLogDto;
import com.radiant.log.access.domain.dto.ExternalAccessLogDto;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.restapi.RestApiUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping({"/api/internal/v1/log/access"})
public class AccessLogController {
   @Autowired
   private AccessLogService accessLogService;

   @GetMapping
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public Page<AccessLogDto> getAccessLogs(@RequestParam(name = "startDate",required = false) Long start, @RequestParam(name = "endDate",required = false) Long end, @RequestParam(required = false) String appId, @RequestParam(required = false) String sysId, @RequestParam(required = false) String clientIp, @RequestParam(required = false) String username, @RequestParam(required = false) String userId, @RequestParam(required = false) String path, @RequestParam(required = false) Integer responseCode, @RequestParam(required = false) Integer duration, Pageable pageable) {
      return this.accessLogService.getLogs(start, end, appId, sysId, clientIp, username, userId, path, responseCode, duration, pageable);
   }

   @GetMapping({"/download"})
   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   public ResponseEntity<StreamingResponseBody> downloadAccessLogs(@RequestParam(required = false) Long startDate, @RequestParam(required = false) Long endDate, @RequestParam(required = false) String apId, @RequestParam(required = false) String sysId, @RequestParam(required = false) String clientIp, @RequestParam(required = false) String username, @RequestParam(required = false) String userId, @RequestParam(required = false) String path, @RequestParam(required = false) Integer responseCode, @RequestParam(required = false) Integer duration) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
      String fileName = String.format("access_logs-%s.csv", dateFormat.format(new Date()));
      MediaType mediaType = MediaType.parseMediaType("text/csv");
      HttpHeaders headers = RestApiUtils.getHttpResponseAttachmentHeaders(fileName);
      StreamingResponseBody streamingResponse = (outputStream) -> this.accessLogService.downloadLogs(outputStream, startDate, endDate, apId, sysId, clientIp, username, userId, path, responseCode, duration);
      ResponseEntity.BodyBuilder response = ((ResponseEntity.BodyBuilder)ResponseEntity.ok().headers(headers)).contentType(mediaType);
      return response.body(streamingResponse);
   }

   @GetMapping({"/external"})
   public List<ExternalAccessLogDto> getFilteredAccessLogs(@RequestParam(name = "startTime") String start, @RequestParam(name = "endTime",required = false) String end) {
      return this.accessLogService.getFilteredLogs(start, end);
   }
}
