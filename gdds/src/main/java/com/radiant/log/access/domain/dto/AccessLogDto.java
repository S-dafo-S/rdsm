package com.radiant.log.access.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.log.access.domain.AccessLog;
import com.radiant.util.DateUtils;
import java.util.Date;

public class AccessLogDto {
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date date;
   private String appId;
   private String sysId;
   private String clientIp;
   private String userId;
   private String username;
   private String apiPath;
   private String queryBody;
   private String queryParams;
   private String readFileName;
   private String responseCode;
   private String errorMessage;
   private long duration;

   public AccessLogDto(AccessLog log) {
      this.date = log.getStartTime();
      this.appId = log.getAppId();
      this.sysId = log.getSysId();
      this.clientIp = log.getClientAddress();
      this.userId = log.getUserId();
      this.username = log.getUserName();
      this.apiPath = log.getApiPath();
      this.queryBody = log.getQueryBody();
      this.queryParams = log.getQueryParams();
      this.readFileName = log.getReadFileName();
      this.responseCode = log.getResponseCode().toString();
      this.errorMessage = log.getErrorMessage();
      this.duration = log.getDuration();
   }

   public Date getDate() {
      return DateUtils.cloneDate(this.date);
   }

   public AccessLogDto() {
   }

   public String getAppId() {
      return this.appId;
   }

   public String getSysId() {
      return this.sysId;
   }

   public String getClientIp() {
      return this.clientIp;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getUsername() {
      return this.username;
   }

   public String getApiPath() {
      return this.apiPath;
   }

   public String getQueryBody() {
      return this.queryBody;
   }

   public String getQueryParams() {
      return this.queryParams;
   }

   public String getReadFileName() {
      return this.readFileName;
   }

   public String getResponseCode() {
      return this.responseCode;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public long getDuration() {
      return this.duration;
   }

   public void setAppId(final String appId) {
      this.appId = appId;
   }

   public void setSysId(final String sysId) {
      this.sysId = sysId;
   }

   public void setClientIp(final String clientIp) {
      this.clientIp = clientIp;
   }

   public void setUserId(final String userId) {
      this.userId = userId;
   }

   public void setUsername(final String username) {
      this.username = username;
   }

   public void setApiPath(final String apiPath) {
      this.apiPath = apiPath;
   }

   public void setQueryBody(final String queryBody) {
      this.queryBody = queryBody;
   }

   public void setQueryParams(final String queryParams) {
      this.queryParams = queryParams;
   }

   public void setReadFileName(final String readFileName) {
      this.readFileName = readFileName;
   }

   public void setResponseCode(final String responseCode) {
      this.responseCode = responseCode;
   }

   public void setErrorMessage(final String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public void setDuration(final long duration) {
      this.duration = duration;
   }
}
