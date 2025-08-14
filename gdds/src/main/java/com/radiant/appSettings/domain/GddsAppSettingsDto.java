package com.radiant.appSettings.domain;

public class GddsAppSettingsDto {
   private String sysId;

   public GddsAppSettingsDto(String sysId) {
      this.sysId = sysId;
   }

   public GddsAppSettingsDto() {
   }

   public String getSysId() {
      return this.sysId;
   }

   public void setSysId(final String sysId) {
      this.sysId = sysId;
   }
}
