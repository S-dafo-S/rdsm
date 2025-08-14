package com.radiant.dataSharingSystem.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.build.domain.dto.DssUpgradeInfoDto;
import com.radiant.court.domain.dto.GddsCourtDto;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.util.DateUtils;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class DssDto {
   private Long id;
   private String name;
   private GddsCourtDto deployCourt;
   private String version;
   private Boolean connectStatus;
   private Long accessAll;
   private Long accessFail;
   private DssUpgradeInfoDto upgradeInfo;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private boolean editable;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;

   public DssDto(DNode dnode) {
      this.id = dnode.getId();
      this.name = dnode.getName();
      this.deployCourt = new GddsCourtDto(dnode.getDeployCourt());
      this.editable = StringUtils.isEmpty(dnode.getDnodeUrl());
      this.creationDate = dnode.getCreationDate();
      this.version = dnode.getVersion();
      this.connectStatus = dnode.getConnectStatus();
      this.accessAll = dnode.getAccessAll();
      this.accessFail = dnode.getAccessFail();
   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public GddsCourtDto getDeployCourt() {
      return this.deployCourt;
   }

   public String getVersion() {
      return this.version;
   }

   public Boolean getConnectStatus() {
      return this.connectStatus;
   }

   public Long getAccessAll() {
      return this.accessAll;
   }

   public Long getAccessFail() {
      return this.accessFail;
   }

   public DssUpgradeInfoDto getUpgradeInfo() {
      return this.upgradeInfo;
   }

   public boolean isEditable() {
      return this.editable;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setDeployCourt(final GddsCourtDto deployCourt) {
      this.deployCourt = deployCourt;
   }

   public void setVersion(final String version) {
      this.version = version;
   }

   public void setConnectStatus(final Boolean connectStatus) {
      this.connectStatus = connectStatus;
   }

   public void setAccessAll(final Long accessAll) {
      this.accessAll = accessAll;
   }

   public void setAccessFail(final Long accessFail) {
      this.accessFail = accessFail;
   }

   public void setUpgradeInfo(final DssUpgradeInfoDto upgradeInfo) {
      this.upgradeInfo = upgradeInfo;
   }

   @JsonProperty(
      access = Access.READ_ONLY
   )
   public void setEditable(final boolean editable) {
      this.editable = editable;
   }

   public DssDto() {
   }
}
