package com.radiant.log.access.domain;

import com.radiant.util.DateUtils;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(
   name = "access_log",
   indexes = {@Index(
   name = "access_log_date_idx",
   columnList = "start_time"
)}
)
public class AccessLog {
   @Id
   @Column(
      name = "id",
      nullable = false,
      updatable = false
   )
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   private Long id;
   @Column(
      name = "app_id"
   )
   private String appId;
   @Column(
      name = "sys_id"
   )
   private String sysId;
   @Column(
      name = "server_address",
      nullable = false
   )
   private String serverAddress;
   @Column(
      name = "server_port"
   )
   private Integer serverPort;
   @Column(
      name = "api_path",
      nullable = false
   )
   private String apiPath;
   @Column(
      name = "client_address",
      nullable = false
   )
   private String clientAddress;
   @Column(
      name = "org_name"
   )
   private String orgName;
   @Column(
      name = "org_id"
   )
   private String orgId;
   @Column(
      name = "user_name"
   )
   private String userName;
   @Column(
      name = "query_body",
      length = 10240
   )
   private String queryBody;
   @Column(
      name = "query_params",
      length = 10240
   )
   private String queryParams;
   @Column(
      name = "read_file_name"
   )
   private String readFileName;
   @Column(
      name = "user_id"
   )
   private String userId;
   @Column(
      name = "sc_request_id"
   )
   private String scRequestId;
   @Column(
      name = "start_time",
      nullable = false
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date startTime;
   @Column(
      name = "end_time",
      nullable = false
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date endTime;
   @Column(
      name = "duration",
      nullable = false
   )
   private Long duration;
   @Column(
      name = "response_code",
      nullable = false
   )
   private Integer responseCode;
   @Column(
      name = "response_length"
   )
   private Integer responseLength;
   @Column(
      name = "error_message"
   )
   private String errorMessage;

   public Date getStartTime() {
      return DateUtils.cloneDate(this.startTime);
   }

   public void setStartTime(Date startTime) {
      this.startTime = DateUtils.cloneDate(startTime);
   }

   public Date getEndTime() {
      return DateUtils.cloneDate(this.endTime);
   }

   public void setEndTime(Date endTime) {
      this.endTime = DateUtils.cloneDate(endTime);
   }

   public Long getId() {
      return this.id;
   }

   public String getAppId() {
      return this.appId;
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

   public String getOrgName() {
      return this.orgName;
   }

   public String getOrgId() {
      return this.orgId;
   }

   public String getUserName() {
      return this.userName;
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

   public String getUserId() {
      return this.userId;
   }

   public String getScRequestId() {
      return this.scRequestId;
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

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setAppId(final String appId) {
      this.appId = appId;
   }

   public void setSysId(final String sysId) {
      this.sysId = sysId;
   }

   public void setServerAddress(final String serverAddress) {
      this.serverAddress = serverAddress;
   }

   public void setServerPort(final Integer serverPort) {
      this.serverPort = serverPort;
   }

   public void setApiPath(final String apiPath) {
      this.apiPath = apiPath;
   }

   public void setClientAddress(final String clientAddress) {
      this.clientAddress = clientAddress;
   }

   public void setOrgName(final String orgName) {
      this.orgName = orgName;
   }

   public void setOrgId(final String orgId) {
      this.orgId = orgId;
   }

   public void setUserName(final String userName) {
      this.userName = userName;
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

   public void setUserId(final String userId) {
      this.userId = userId;
   }

   public void setScRequestId(final String scRequestId) {
      this.scRequestId = scRequestId;
   }

   public void setDuration(final Long duration) {
      this.duration = duration;
   }

   public void setResponseCode(final Integer responseCode) {
      this.responseCode = responseCode;
   }

   public void setResponseLength(final Integer responseLength) {
      this.responseLength = responseLength;
   }

   public void setErrorMessage(final String errorMessage) {
      this.errorMessage = errorMessage;
   }
}
