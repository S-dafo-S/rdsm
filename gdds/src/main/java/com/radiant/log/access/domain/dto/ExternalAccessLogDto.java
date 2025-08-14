package com.radiant.log.access.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.radiant.log.access.domain.AccessLog;
import java.text.SimpleDateFormat;

public class ExternalAccessLogDto {
   @JsonProperty("b_sys_id")
   private String sysId;
   @JsonProperty("service_ip")
   private String serverAddress;
   @JsonProperty("service_port")
   private Integer serverPort;
   @JsonProperty("service_path")
   private String apiPath;
   @JsonProperty("client_ip")
   private String clientAddress;
   @JsonProperty("court_name")
   private String courtName;
   @JsonProperty("court_id")
   private String courtId;
   @JsonProperty("user_name")
   private String userName;
   @JsonProperty("user_id")
   private String userId;
   @JsonProperty("start_time")
   private String startTime;
   @JsonProperty("end_time")
   private String endTime;
   @JsonProperty("duration")
   private Long duration;
   @JsonProperty("response_code")
   private Integer responseCode;
   @JsonProperty("response_length")
   private Integer responseLength;

   public ExternalAccessLogDto(AccessLog log) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      this.sysId = log.getSysId();
      this.serverAddress = log.getServerAddress();
      this.serverPort = log.getServerPort();
      this.apiPath = log.getApiPath();
      this.clientAddress = log.getClientAddress();
      this.courtName = log.getOrgName();
      this.courtId = log.getOrgId();
      this.userName = log.getUserName();
      this.userId = log.getUserId();
      this.startTime = dateFormat.format(log.getStartTime());
      this.endTime = dateFormat.format(log.getEndTime());
      this.duration = log.getEndTime().getTime() - log.getStartTime().getTime();
      this.responseCode = log.getResponseCode();
      this.responseLength = log.getResponseLength();
   }

   public ExternalAccessLogDto() {
   }

   public String getSysId() {
      return this.sysId;
   }

   public String getServerAddress() {
      return this.serverAddress;
   }

   public Integer getServerPort() {
      return this.serverPort;
   }

   public String getApiPath() {
      return this.apiPath;
   }

   public String getClientAddress() {
      return this.clientAddress;
   }

   public String getCourtName() {
      return this.courtName;
   }

   public String getCourtId() {
      return this.courtId;
   }

   public String getUserName() {
      return this.userName;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getStartTime() {
      return this.startTime;
   }

   public String getEndTime() {
      return this.endTime;
   }

   public Long getDuration() {
      return this.duration;
   }

   public Integer getResponseCode() {
      return this.responseCode;
   }

   public Integer getResponseLength() {
      return this.responseLength;
   }
}
