package com.radiant.appSettings;

import com.radiant.DSSReleaseInfo;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.build.ReleaseInfoService;
import com.radiant.build.domain.Upgrade;
import com.radiant.build.domain.repository.UpgradeRepository;
import com.radiant.build.service.ReleaseCompatibilityResponseDto;
import com.radiant.exception.court.FetchCourtListException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReleaseInfoServiceImpl implements ReleaseInfoService {
   @Autowired
   private DSSReleaseInfo DSSReleaseInfo;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private UpgradeRepository upgradeRepository;

   public String getVersion() {
      return this.DSSReleaseInfo.getReleaseVersion();
   }

   public String getName() {
      return "DSS";
   }

   public Set<String> getCompatibleVersions() {
      String gddsUrl = this.applicationPropertyService.getStringValue("gdds_url");
      if (gddsUrl == null) {
         return Collections.emptySet();
      } else {
         ResponseEntity<ReleaseCompatibilityResponseDto> responseEntity = this.restTemplate.exchange(gddsUrl + String.format("/api/public/v1/release/compatible?version=%s", this.DSSReleaseInfo.getReleaseVersion()), HttpMethod.GET, (HttpEntity)null, new ParameterizedTypeReference<ReleaseCompatibilityResponseDto>() {
         }, new Object[0]);
         ReleaseCompatibilityResponseDto body = (ReleaseCompatibilityResponseDto)responseEntity.getBody();
         if (body == null) {
            throw new FetchCourtListException();
         } else {
            return new HashSet(body.getCompatibleDssVersions());
         }
      }
   }

   public List<Upgrade> getBackupVersions() {
      return this.upgradeRepository.findAll();
   }

   public String getMaxDSSsVersion() {
      return null;
   }

   public String getMinDSSsVersion() {
      return null;
   }

   public String getLatestAllowedRollbackDSSVersion() {
      return null;
   }

   public String getGDDSReleaseServerUrl() {
      return this.applicationPropertyService.getStringValue("gdds_url") + "/media/releases";
   }

   public List<String> getCompatibleDssVersions() {
      return new ArrayList(this.getCompatibleVersions());
   }
}
