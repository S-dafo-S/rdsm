package com.radiant.auth.domain;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import com.radiant.util.DateUtils;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(
   name = "app_token"
)
public class ApplicationToken {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   @Column(
      name = "id"
   )
   private Long id;
   @ManyToOne
   @JoinColumn(
      name = "application",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "app_token_app_fk"
)
   )
   private ApplicationRegistry application;
   @Column(
      name = "token",
      nullable = false,
      length = 1024
   )
   private String token;
   @Column(
      name = "username",
      nullable = false
   )
   private String username;
   @Column(
      name = "user_id",
      nullable = false
   )
   private String userId;
   @Column(
      name = "org_name"
   )
   private String orgName;
   @Column(
      name = "org_id"
   )
   private String orgId;
   @Column(
      name = "system_id"
   )
   private String externalSystemId;
   @Column(
      name = "authentication_time"
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date authenticationTime;
   @Column(
      name = "last_query_time"
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date lastQueryTime;
   @Column(
      name = "last_response_time"
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date lastResponseTime;

   public ApplicationToken(ApplicationRegistry application, String token, String username, String userId, @NotNull Date authenticationTime) {
      this.application = application;
      this.token = token;
      this.username = username;
      this.userId = userId;
      this.authenticationTime = DateUtils.cloneDate(authenticationTime);
   }

   public Date getAuthenticationTime() {
      return DateUtils.cloneDate(this.authenticationTime);
   }

   public void setAuthenticationTime(Date authenticationTime) {
      this.authenticationTime = DateUtils.cloneDate(authenticationTime);
   }

   public Date getLastQueryTime() {
      return DateUtils.cloneDate(this.lastQueryTime);
   }

   public void setLastQueryTime(Date lastQueryTime) {
      this.lastQueryTime = DateUtils.cloneDate(lastQueryTime);
   }

   public Date getLastResponseTime() {
      return DateUtils.cloneDate(this.lastResponseTime);
   }

   public void setLastResponseTime(Date lastResponseTime) {
      this.lastResponseTime = DateUtils.cloneDate(lastResponseTime);
   }

   public Long getId() {
      return this.id;
   }

   public ApplicationRegistry getApplication() {
      return this.application;
   }

   public String getToken() {
      return this.token;
   }

   public String getUsername() {
      return this.username;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getOrgName() {
      return this.orgName;
   }

   public String getOrgId() {
      return this.orgId;
   }

   public String getExternalSystemId() {
      return this.externalSystemId;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setApplication(final ApplicationRegistry application) {
      this.application = application;
   }

   public void setToken(final String token) {
      this.token = token;
   }

   public void setUsername(final String username) {
      this.username = username;
   }

   public void setUserId(final String userId) {
      this.userId = userId;
   }

   public void setOrgName(final String orgName) {
      this.orgName = orgName;
   }

   public void setOrgId(final String orgId) {
      this.orgId = orgId;
   }

   public void setExternalSystemId(final String externalSystemId) {
      this.externalSystemId = externalSystemId;
   }

   public ApplicationToken() {
   }
}
