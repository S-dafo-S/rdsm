package com.radiant.query.service;

import com.radiant.applicationRegistry.service.ApplicationRegistryService;
import com.radiant.auth.service.AccessKeyAuthenticationService;
import com.radiant.court.domain.GddsCourt;
import com.radiant.court.service.GddsCourtService;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import com.radiant.exception.dataSharingSystem.DSSConnectionException;
import com.radiant.exception.dataSharingSystem.DataSharingSystemIsNotConnected;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.util.TransactionUtils;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GddsQueryExecutionServiceImpl implements GddsQueryExecutionService {
   @Autowired
   private GddsCourtService gddsCourtService;
   @Autowired
   private DataSharingSystemService dataSharingSystemService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private AccessKeyAuthenticationService accessKeyAuthenticationService;
   @Autowired
   private AccessLogService accessLogService;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;
   @Autowired
   private ApplicationRegistryService applicationRegistryService;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;

     public String execute(Long id, String queryName, Map<String, String> params, HttpServletRequest request, Boolean isDssId) {
        String responseBody;
        try {
         this.applicationRegistryService.validateApiAccess(request, queryName);
         Long courtId;
         DNode targetDNode;
         if (isDssId) {
            targetDNode = this.dataSharingSystemService.getDss(id);
            GddsCourt court = targetDNode.getDeployCourt();
            courtId = court.getId();
         } else {
            GddsCourt court = this.gddsCourtService.getCourt(id);
            List<DNode> dnodes = this.dataSharingSystemService.getByCourtAndValidate(court);
            if (dnodes.size() > 1) {
               throw new NotImplementedException("Multiple Court-DSS is not supported yet");
            }

            targetDNode = (DNode)dnodes.get(0);
            courtId = id;
         }

         if (targetDNode.getDnodeUrl() == null) {
            throw new DataSharingSystemIsNotConnected(targetDNode.getId());
         }

         this.applicationRegistryService.validateDssAccess(request, Collections.singleton(targetDNode.getId()));
         URI url = this.constructUri(courtId, targetDNode, queryName, params);

         try {
            HttpHeaders headers = new HttpHeaders();
            if (targetDNode.getToken() != null) {
               headers.setBearerAuth(targetDNode.getToken());
            }

            ResponseEntity<String> response = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers), String.class), (Logger)null, "query execution");
              String result = (String)response.getBody();
              this.accessLogService.logSuccess(result != null ? result.length() : null, (ExternalProgramBody)null);
              this.dnodeAccessLogService.log(targetDNode, url.toString(), true, (String)null);
              this.serviceLogManagementService.log(LogLevel.INFO, (String)null, HttpStatus.OK.value(), result != null ? result.length() : null);
              responseBody = result;
         } catch (ResourceAccessException exc) {
            this.dnodeAccessLogService.log(targetDNode, url.toString(), false, exc.getMessage());
            throw new DSSConnectionException(targetDNode.getName(), targetDNode.getDnodeUrl());
         } catch (Exception e) {
            this.dnodeAccessLogService.log(targetDNode, url.toString(), false, e.getMessage());
            throw e;
         }
        } finally {
           this.accessKeyAuthenticationService.updateResponseTime(request.getHeader("Authorization"));
        }

        return responseBody;
     }

   private URI constructUri(Long courtId, DNode dnode, String queryName, Map<String, String> params) {
      String url = dnode.getDnodeUrl() + "/api/v1/query/" + courtId + "/" + queryName;
      UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
      params.forEach((x$0, xva$1) -> builder.queryParam(x$0, new Object[]{xva$1}));
      return builder.build().toUri();
   }
}
