package com.radiant.appSettings.service;

import com.radiant.appSettings.domain.TokenSettingsDto;
import com.radiant.applicationProperty.domain.ApplicationProperty;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.JwtTokenService;
import com.radiant.util.TransactionUtils;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
@Transactional
public class DssAppSettingsServiceImpl extends AppSettingsServiceImpl implements DssAppSettingsService {
   private static final Logger LOG = LoggerFactory.getLogger(DssAppSettingsServiceImpl.class);
   @Autowired
   private ApplicationPropertyService propertyService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private JwtTokenService jwtTokenService;

   public TokenSettingsDto getTokenSetting() {
      return new TokenSettingsDto(this.propertyService.getStringValue("dss_id"), this.propertyService.getStringValue("token"), this.propertyService.getStringValue("gdds_token"), false);
   }

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public TokenSettingsDto updateTokenSettings(TokenSettingsDto request) {
      if (request.getGenerate()) {
         String newToken = this.jwtTokenService.generateToken();
         String gddsToken = this.propertyService.getStringValue("gdds_token");
         String url = this.propertyService.getStringValue("gdds_url") + "/api/internal/v1/settings/qnode/token";
         request.setGenerate(false);
         request.setDnodeToken(newToken);
         request.setDssAccountId(this.propertyService.getStringValue("dss_id"));
         HttpHeaders headers = new HttpHeaders();
         if (gddsToken != null) {
            headers.setBearerAuth(gddsToken);
         }

         HttpEntity<TokenSettingsDto> entity = new HttpEntity(request, headers);
         ResponseEntity<TokenSettingsDto> response = (ResponseEntity)TransactionUtils.doWithCheckForUndesirableActiveTransaction(() -> this.restTemplate.postForEntity(url, entity, TokenSettingsDto.class, new Object[0]), LOG, "updateTokenSettings");
         if (response.getBody() != null) {
            this.propertyService.updateValues(Collections.singletonList(new ApplicationProperty("token", newToken, (String)null)));
         }

         return this.getTokenSetting();
      } else {
         this.propertyService.updateValues(Collections.singletonList(new ApplicationProperty("gdds_token", request.getQnodeToken(), (String)null)));
         return this.getTokenSetting();
      }
   }
}
