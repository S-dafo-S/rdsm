package com.radiant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(
   value = {"classpath:release_info.json"},
   factory = JsonPropertySourceFactory.class
)
@ConfigurationProperties
public class DSSReleaseInfo {
   private String releaseVersion;

   public String getReleaseVersion() {
      return this.releaseVersion;
   }

   public void setReleaseVersion(final String releaseVersion) {
      this.releaseVersion = releaseVersion;
   }
}
