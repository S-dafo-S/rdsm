package com.radiant.release;

import com.radiant.GDDSReleaseInfo;
import com.radiant.build.ReleaseInfoEventService;
import com.radiant.build.ReleaseInfoService;
import com.radiant.build.domain.Upgrade;
import com.radiant.build.domain.repository.UpgradeRepository;
import com.radiant.build.service.ReleaseCompatibilityResponseDto;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.domain.DnodeRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReleaseInfoServiceImpl implements ReleaseInfoService, ReleaseInfoEventService {
   private static final String DEFAULT_VERSION = "1.0.0";
   @Autowired
   private GDDSReleaseInfo GDDSReleaseInfo;
   @Autowired
   private UpgradeRepository upgradeRepository;
   @Autowired
   private DnodeRepository dnodeRepository;

   public String getVersion() {
      return this.GDDSReleaseInfo.getVersion();
   }

   public String getName() {
      return "GDDS";
   }

   public Set<String> getCompatibleVersions() {
      return Collections.emptySet();
   }

   public List<Upgrade> getBackupVersions() {
      return this.upgradeRepository.findAll();
   }

   public String getMaxDSSsVersion() {
      return (String)this.getMaxVersionDss().map(DNode::getVersion).orElse("1.0.0");
   }

   public String getMinDSSsVersion() {
      return (String)this.getMinVersionDss().map(DNode::getVersion).orElse("1.0.0");
   }

   public String getLatestAllowedRollbackDSSVersion() {
      return this.GDDSReleaseInfo.getVersion().equals("1.0.0") ? null : (String)this.GDDSReleaseInfo.getCompatibleDssVersions().stream().filter((v) -> !Objects.equals(v, this.GDDSReleaseInfo.getVersion())).max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalArgumentException("GDDS must at least support one backward DSS version!"));
   }

   public String getGDDSReleaseServerUrl() {
      return "http://172.17.0.1:80/media/releases";
   }

     public ReleaseCompatibilityResponseDto isCompatible(String dssVersion) {
        boolean isCompatible = this.GDDSReleaseInfo.getCompatibleDssVersions().stream()
            .anyMatch(dssVersion::equals);
        return new ReleaseCompatibilityResponseDto(isCompatible, this.GDDSReleaseInfo);
     }

   public List<String> getCompatibleDssVersions() {
      return this.GDDSReleaseInfo.getCompatibleDssVersions();
   }

   public void reportUpdateStatus(Upgrade upgrade) {
   }

   private Optional<DNode> getMaxVersionDss() {
      return this.dnodeRepository.findByVersionNotNull().stream().max(Comparator.comparing(DNode::getVersion));
   }

   private Optional<DNode> getMinVersionDss() {
      return this.dnodeRepository.findByVersionNotNull().stream().min(Comparator.comparing(DNode::getVersion));
   }
}
