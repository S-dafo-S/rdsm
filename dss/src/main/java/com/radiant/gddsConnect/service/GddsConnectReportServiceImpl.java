package com.radiant.gddsConnect.service;

import com.google.common.collect.ImmutableList;
import com.radiant.applicationProperty.domain.ApplicationProperty;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.connect.GddsDssUpdateRequest;
import com.radiant.connect.GddsDssUpdateResponse;
import com.radiant.gddsConnect.domain.dto.GddsConnectDto;
import com.radiant.util.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class GddsConnectReportServiceImpl implements GddsConnectReportService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsConnectReportServiceImpl.class);
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private RestTemplate restTemplate;

   public void reportVersion(GddsConnectDto connectInfo, String releaseVersion) {
      String url = connectInfo.getGddsUrl() + "/api/public/v1/dnode/update";
      GddsDssUpdateRequest updateRequest = GddsDssUpdateRequest.builder().accountId(connectInfo.getAccountId()).accountPassword(this.applicationPropertyService.getStringValue("dss_account_password")).dssUrl(connectInfo.getDssUrl()).version(releaseVersion).build();
      ResponseEntity<GddsDssUpdateResponse> response = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.postForEntity(url, updateRequest, GddsDssUpdateResponse.class, new Object[0]), LOG, "reportVersion");
      GddsDssUpdateResponse body = (GddsDssUpdateResponse)response.getBody();
      if (response.getStatusCode().equals(HttpStatus.OK) && body != null) {
         LOG.info("GDDS response: {}", body.getMessage());
         this.applicationPropertyService.updateValues(ImmutableList.of(new ApplicationProperty("dss_reported_version", releaseVersion, (String)null)));
      } else {
         throw new RuntimeException("Failed to connect");
      }
   }
}
