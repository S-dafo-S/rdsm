package com.radiant.fileAccess.service;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.service.GddsCourtService;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import com.radiant.exception.dataSharingSystem.DataSharingSystemIsNotConnected;
import com.radiant.exception.fileread.DssResourceAccessException;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.util.TransactionUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@ParametersAreNonnullByDefault
public class GddsFileAccessServiceImpl implements GddsFileAccessService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsFileAccessServiceImpl.class);
   private static final String[] PASSTHROUGH_HEADERS = new String[]{"Content-Type", "Content-Disposition", "Content-Length", "Content-Range", "Accept-Ranges"};
   private static final String EXPOSE_HEADERS = (new StringJoiner(",")).add("Accept-Ranges").add("Content-Range").add("Content-Encoding").add("Content-Type").toString();
   private static final String TMP_DIRECTORY = "/tmp/";
   @Autowired
   private GddsCourtService gddsCourtService;
   @Autowired
   private DataSharingSystemService dataSharingSystemService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private AccessLogService accessLogService;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;

   public void read(Long courtId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest initialRequest, boolean saveToTmp) {
      DNode targetDNode = this.getTargetDss(courtId);
      this.readForDss(targetDNode.getId(), logicalPath, internalPath, response, initialRequest, saveToTmp);
   }

   public void readForDss(Long dssId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest initialRequest, boolean saveToTmp) {
      DNode targetDNode = this.dataSharingSystemService.getDss(dssId);
      String url = this.buildUrl(targetDNode, logicalPath, internalPath, initialRequest, true, "asc", 0);
      String dssToken = targetDNode.getToken();
      String rangeHeader = initialRequest.getHeader("Range");
      HttpMethod method = (HttpMethod)Optional.ofNullable(HttpMethod.resolve(initialRequest.getMethod())).orElse(HttpMethod.GET);
      ExternalProgramBody fakeBodyToLog = new ExternalProgramBody();

      try {
         TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.execute(url, method, (request) -> {
               if (dssToken != null) {
                  request.getHeaders().setBearerAuth(dssToken);
               }

               if (rangeHeader != null) {
                  request.getHeaders().add("Range", rangeHeader);
               }

            }, (clientHttpResponse) -> {
               if (saveToTmp) {
                  File tempFile = new File("/tmp/", internalPath);

                  try {
                     FileUtils.copyInputStreamToFile(clientHttpResponse.getBody(), tempFile);
                     return null;
                  } catch (Exception e) {
                     LOG.error("Failed to tmp file {}", internalPath, e);
                  }
               }

               HttpHeaders incomingHeaders = clientHttpResponse.getHeaders();
               response.setStatus(clientHttpResponse.getStatusCode().value());

               for(String header : PASSTHROUGH_HEADERS) {
                  List<String> headerValue = incomingHeaders.get(header);
                  if (headerValue != null && !headerValue.isEmpty()) {
                     for(String s : headerValue) {
                        response.setHeader(header, s);
                     }
                  } else {
                     response.setHeader(header, (String)null);
                  }
               }

               response.setHeader("Access-Control-Expose-Headers", EXPOSE_HEADERS);
               response.setHeader("Access-Control-Allow-Headers", "*");
               response.setHeader("Access-Control-Allow-Origin", "*");
               StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
               int contentLength = StringUtils.isNoneEmpty(new CharSequence[]{response.getHeader("Content-Length")}) ? Integer.parseInt(response.getHeader("Content-Length")) : 0;
               this.accessLogService.logSuccess(contentLength, fakeBodyToLog);
               this.dnodeAccessLogService.log(targetDNode, url, true, (String)null);
               this.serviceLogManagementService.log(LogLevel.INFO, fakeBodyToLog.toString(), HttpStatus.OK.value(), contentLength);
               return null;
            }, new Object[0]), LOG, "read file");
      } catch (ResourceAccessException exc) {
         LOG.error("Failed read file from DSS: connection failed or interrupted", exc);
         throw new DssResourceAccessException(exc, targetDNode.getName(), targetDNode.getDnodeUrl());
      } catch (Exception ex) {
         this.dnodeAccessLogService.log(targetDNode, url, false, ex.getMessage());
         throw ex;
      }
   }

   public void read(Long courtId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest request) {
      this.read(courtId, logicalPath, internalPath, response, request, false);
   }

   public List<String> merge(Long courtId, String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit) {
      DNode targetDNode = this.getTargetDss(courtId);
      return this.doMerge(targetDNode, logicalPath, internalPath, request, sort, limit);
   }

   public List<String> mergeForDss(Long dssId, String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit) {
      DNode targetDNode = this.dataSharingSystemService.getDss(dssId);
      return this.doMerge(targetDNode, logicalPath, internalPath, request, sort, limit);
   }

   private List<String> doMerge(DNode targetDNode, String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit) {
      String url = this.buildUrl(targetDNode, logicalPath, internalPath, request, false, sort, limit);
      String dssToken = targetDNode.getToken();
      HttpHeaders headers = new HttpHeaders();
      if (dssToken != null) {
         headers.setBearerAuth(dssToken);
      }

      ExternalProgramBody fakeBodyToLog = new ExternalProgramBody();

      try {
         ResponseEntity<List<String>> responseEntity = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers), new ParameterizedTypeReference<List<String>>() {
            }, new Object[0]), LOG, "merge file");
         this.accessLogService.logSuccess(0, fakeBodyToLog);
         this.dnodeAccessLogService.log(targetDNode, url, true, (String)null);
         this.serviceLogManagementService.log(LogLevel.INFO, fakeBodyToLog.toString(), HttpStatus.OK.value(), 0);
         return (List)responseEntity.getBody();
      } catch (Exception ex) {
         this.dnodeAccessLogService.log(targetDNode, url, false, ex.getMessage());
         throw ex;
      }
   }

   private String buildUrl(DNode targetDNode, String logicalPath, String internalPath, HttpServletRequest request, boolean isRead, String sort, int limit) {
      if (targetDNode.getDnodeUrl() == null) {
         throw new DataSharingSystemIsNotConnected(targetDNode.getId());
      } else {
         String postCommand = isRead ? "" : "merge";
         Map<String, String> uriVariables = new HashMap();
         uriVariables.put("logicalPath", logicalPath);
         uriVariables.put("internalPath", internalPath);
         UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetDNode.getDnodeUrl()).path("/api/v1/readfile").pathSegment(new String[]{postCommand}).path("/{logicalPath}/{internalPath}");
         String customIp = (String)request.getAttribute("CUSTOM_IP_ADDRESS");
         if (StringUtils.isNotEmpty(customIp)) {
            uriBuilder.queryParam("ip", new Object[]{customIp});
         }

         if (!isRead) {
            uriBuilder.queryParam("sort", new Object[]{sort});
            uriBuilder.queryParam("limit", new Object[]{limit});
         }

         return uriBuilder.buildAndExpand(uriVariables).toUriString();
      }
   }

   private DNode getTargetDss(Long courtId) {
      GddsCourt court = this.gddsCourtService.getCourt(courtId);
      List<DNode> dnodeList = this.dataSharingSystemService.getByCourtAndValidate(court);
      if (dnodeList.size() > 1) {
         throw new NotImplementedException("Multiple Court-DSS is not supported yet");
      } else {
         return (DNode)dnodeList.get(0);
      }
   }
}
