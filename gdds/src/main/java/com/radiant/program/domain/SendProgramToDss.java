package com.radiant.program.domain;

import com.radiant.applicationRegistry.service.ApplicationRegistryService;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.dataSharingSystem.DSSConnectionException;
import com.radiant.exception.dataSharingSystem.DataSharingSystemIsNotConnected;
import com.radiant.exception.program.DssRestClientException;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import com.radiant.util.TransactionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SendProgramToDss extends ProgramStage {
   private static final Logger LOG = LoggerFactory.getLogger(SendProgramToDss.class);
   private static final Set<String> EXCLUDED_HEADERS = new HashSet(Arrays.asList("Authorization", "Host", "Content-Type"));
   private final DataSharingSystemService dataSharingSystemService;
   private final RestTemplate restTemplate;
   private final ApplicationRegistryService applicationRegistryService;
   private final DnodeAccessLogService dnodeAccessLogService;

   public SendProgramToDss(DataSharingSystemService dataSharingSystemService, RestTemplate restTemplate, DnodeAccessLogService dnodeAccessLogService, ApplicationRegistryService applicationRegistryService) {
      this.dataSharingSystemService = dataSharingSystemService;
      this.restTemplate = restTemplate;
      this.applicationRegistryService = applicationRegistryService;
      this.dnodeAccessLogService = dnodeAccessLogService;
   }

   void handleRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest) {
      Map<String, DNode> baseDss = new HashMap();
      List<DNode> allDNodes = this.dataSharingSystemService.getAllDss();
      if (request.getCustomDnodeUrl().isEmpty()) {
         for(DNode dnode : allDNodes) {
            if (dnode.getDnodeUrl() == null) {
               throw new DataSharingSystemIsNotConnected(dnode.getId());
            }

            baseDss.putIfAbsent(dnode.getDnodeUrl(), dnode);
         }
      } else {
         for(String dssUrl : request.getCustomDnodeUrl()) {
            allDNodes.stream().filter((dss) -> dssUrl != null && dssUrl.equalsIgnoreCase(dss.getDnodeUrl())).findFirst().ifPresent((foundDss) -> {
               DNode existingDNode = baseDss.putIfAbsent(dssUrl, foundDss);
            });
         }
      }

      this.applicationRegistryService.validateDssAccess(originalRequest, (Set)baseDss.values().stream().map(DNode::getId).collect(Collectors.toSet()));
      baseDss.forEach((dssUrlx, dss) -> {
         URI url = this.constructUri(dssUrlx, request.getProgramName(), request.getUrlParams());

         try {
            HttpHeaders headers = new HttpHeaders();
            if (dss.getToken() != null) {
               headers.setBearerAuth(dss.getToken());
            } else {
               headers.setBearerAuth("");
            }

            headers.add("Content-Type", "application/json");

            for(Map.Entry<String, String> entry : request.getHeaderParams().entrySet()) {
               if (!EXCLUDED_HEADERS.contains(entry.getKey())) {
                  headers.add((String)entry.getKey(), (String)entry.getValue());
               }
            }

            HttpEntity<ProgramRequest> requestEntity = new HttpEntity(request, headers);
            if (request.getContentType().equals("application/json")) {
               ResponseEntity<ProgramResponse> responseEntity = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, ProgramResponse.class), LOG, "SendProgramToDss");
               this.dnodeAccessLogService.log(dss, url.toString(), true, (String)null);
               ProgramResponse body = (ProgramResponse)responseEntity.getBody();
               if (body != null && body.getBody() != null) {
                  response.addAllResult(body.getBody());
               }
            } else if (request.getContentType().equals("application/octet-stream")) {
               if (baseDss.size() > 1) {
                  throw new NotImplementedException("Reading file is not allowed for multiple DSSs distributed court");
               }

               ResponseEntity<Resource> responseEntity = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, Resource.class), LOG, "SendProgramToDss");
               this.dnodeAccessLogService.log(dss, url.toString(), true, (String)null);
                 if (responseEntity.getBody() != null) {
                    try (InputStream is = responseEntity.getBody().getInputStream()) {
                       StreamUtils.copy(is, response.getServletResponse().getOutputStream());
                    } catch (IOException e) {
                       throw new RdsmIOException(e);
                    }
                 }
            }

         } catch (ResourceAccessException exc) {
            LOG.error("DNode connection error", exc);
            throw new DSSConnectionException(dss.getDnodeUrl());
         } catch (RestClientException e) {
            throw new DssRestClientException(e, dssUrlx);
         } catch (Exception e) {
            this.dnodeAccessLogService.log(dss, url.toString(), false, e.getMessage());
            throw e;
         }
      });
   }

   private URI constructUri(String baseDssUrl, String programName, Map<String, String> params) {
      String url = baseDssUrl + "/api/v1/program/" + programName;
      UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
      params.forEach((x$0, xva$1) -> builder.queryParam(x$0, new Object[]{xva$1}));
      return builder.build().toUri();
   }
}
