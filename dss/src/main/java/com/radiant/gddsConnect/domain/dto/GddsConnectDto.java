package com.radiant.gddsConnect.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.connect.GddsDssConnectRequest;
import com.radiant.util.DateUtils;
import java.util.Date;

public class GddsConnectDto extends GddsDssConnectRequest {
   private Long deployRegionId;
   private String deployRegionName;
   private Long deployCourtId;
   private String deployCourtName;
   private String qnodeDnodeId;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date connectDate;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Boolean needRestart = false;

   public GddsConnectDto(Boolean needRestart) {
      this.needRestart = needRestart;
   }

   public Date getConnectDate() {
      return DateUtils.cloneDate(this.connectDate);
   }

   public void setConnectDate(Date connectDate) {
      this.connectDate = DateUtils.cloneDate(connectDate);
   }

   public Long getDeployRegionId() {
      return this.deployRegionId;
   }

   public String getDeployRegionName() {
      return this.deployRegionName;
   }

   public Long getDeployCourtId() {
      return this.deployCourtId;
   }

   public String getDeployCourtName() {
      return this.deployCourtName;
   }

   public String getQnodeDnodeId() {
      return this.qnodeDnodeId;
   }

   public Boolean getNeedRestart() {
      return this.needRestart;
   }

   public void setDeployRegionId(final Long deployRegionId) {
      this.deployRegionId = deployRegionId;
   }

   public void setDeployRegionName(final String deployRegionName) {
      this.deployRegionName = deployRegionName;
   }

   public void setDeployCourtId(final Long deployCourtId) {
      this.deployCourtId = deployCourtId;
   }

   public void setDeployCourtName(final String deployCourtName) {
      this.deployCourtName = deployCourtName;
   }

   public void setQnodeDnodeId(final String qnodeDnodeId) {
      this.qnodeDnodeId = qnodeDnodeId;
   }

   @JsonProperty(
      access = Access.READ_ONLY
   )
   public void setNeedRestart(final Boolean needRestart) {
      this.needRestart = needRestart;
   }

   public GddsConnectDto() {
   }
}
