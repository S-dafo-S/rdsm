package com.radiant.appSettings.service;

import com.radiant.appSettings.domain.AccessLogSettingsDto;
import com.radiant.appSettings.domain.GddsAppSettingsDto;
import com.radiant.appSettings.domain.TokenSettingsDto;
import com.radiant.applicationProperty.domain.ApplicationProperty;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.dto.DssDetailsDto;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.util.TransactionUtils;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
public class GddsAppSettingsServiceImpl extends AppSettingsServiceImpl implements GddsAppSettingsService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsAppSettingsServiceImpl.class);
   @Autowired
   private ApplicationPropertyService propertyService;
   @Autowired
   private DataSharingSystemService dataSharingSystemService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private JwtTokenService jwtTokenService;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;
   public static final String GDDS_SYS_ID = "sys_id";
   public static final String ACCESS_LOG_RETENTION = "access_log_retention";
   public static final int DEFAULT_ACCESS_LOG_RETENTION = 30;

   public GddsAppSettingsDto getGddsAppSettings() {
      String sysId = this.propertyService.getStringValue("sys_id");
      return new GddsAppSettingsDto(sysId);
   }

   @Transactional
   public GddsAppSettingsDto updateGddsAppSettings(GddsAppSettingsDto request) {
      return this.getGddsAppSettings();
   }

   @Transactional(
      readOnly = true
   )
   public AccessLogSettingsDto getAccessLogSetting() {
      AccessLogSettingsDto result = new AccessLogSettingsDto();
      int retention = this.propertyService.getStringValue("access_log_retention") != null ? Integer.parseInt(this.propertyService.getStringValue("access_log_retention")) : 30;
      result.setRetention(retention);
      return result;
   }

   @Transactional
   public AccessLogSettingsDto updateAccessLogSettings(AccessLogSettingsDto accessLogSettings) {
      List<ApplicationProperty> updatedProps = new ArrayList();
      updatedProps.add(new ApplicationProperty("access_log_retention", String.valueOf(accessLogSettings.getRetention()), (String)null));
      this.propertyService.updateValues(updatedProps);
      return this.getAccessLogSetting();
   }

   @Transactional(
      readOnly = true
   )
   public TokenSettingsDto getTokenSetting(String dssAccountId) {
      DNode found = (DNode)this.dataSharingSystemService.getAllDss().stream().filter((dss) -> dss.getAccountId().equals(dssAccountId)).findFirst().orElse((Object)null);
      return found != null ? new TokenSettingsDto(dssAccountId, found.getToken(), found.getQnodeToken(), false) : null;
   }

   public TokenSettingsDto updateTokenSettings(TokenSettingsDto request) {
      DNode foundDNode = (DNode)this.dataSharingSystemService.getAllDss().stream().filter((dss) -> dss.getAccountId().equals(request.getDssAccountId())).findFirst().orElse((Object)null);
      if (foundDNode != null) {
         if (request.getGenerate()) {
            String newToken = this.jwtTokenService.generateToken();
            foundDNode.setQnodeToken(newToken);
            String url = foundDNode.getDnodeUrl() + "/api/v1/settings/dnode/token";
            request.setGenerate(false);
            request.setQnodeToken(newToken);
            HttpHeaders headers = new HttpHeaders();
            if (foundDNode.getToken() != null) {
               headers.setBearerAuth(foundDNode.getToken());
            }

            HttpEntity<TokenSettingsDto> entity = new HttpEntity(request, headers);

            try {
               ResponseEntity<TokenSettingsDto> response = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.postForEntity(url, entity, TokenSettingsDto.class, new Object[0]), LOG, "updateTokenSettings");
               this.dnodeAccessLogService.log(foundDNode, url, true, (String)null);
               if (response.getBody() != null) {
                  this.dataSharingSystemService.updateSecurity(foundDNode.getId(), new DssDetailsDto(foundDNode));
               }

               return this.getTokenSetting(request.getDssAccountId());
            } catch (Exception ex) {
               this.dnodeAccessLogService.log(foundDNode, url, false, ex.getMessage());
               throw ex;
            }
         } else {
            DssDetailsDto req = new DssDetailsDto(foundDNode);
            req.setToken(request.getDnodeToken());
            this.dataSharingSystemService.updateSecurity(foundDNode.getId(), req);
            return this.getTokenSetting(request.getDssAccountId());
         }
      } else {
         return null;
      }
   }
}
