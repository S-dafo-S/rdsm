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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
   name = "app_header"
)
public class ApplicationHeader {
   public static final String X_KSP_REQUEST_ID_KEY = "X-KSP-Request-Id";
   public static final String GATEWAY_TOKEN_KEY = "token";
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
   name = "app_header_app_fk"
)
   )
   private ApplicationRegistry application;
   @Column(
      name = "header",
      nullable = false
   )
   private String header;
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
      name = "created"
   )
   @Temporal(TemporalType.TIMESTAMP)
   @CreationTimestamp
   private Date creationDate;
   @Column(
      name = "expiration"
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date expirationDate;

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public void setCreationDate(Date creationDate) {
      this.creationDate = DateUtils.cloneDate(creationDate);
   }

   public Date getExpirationDate() {
      return DateUtils.cloneDate(this.expirationDate);
   }

   public void setExpirationDate(Date expirationDate) {
      this.expirationDate = DateUtils.cloneDate(expirationDate);
   }

   public Long getId() {
      return this.id;
   }

   public ApplicationRegistry getApplication() {
      return this.application;
   }

   public String getHeader() {
      return this.header;
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

   public void setHeader(final String header) {
      this.header = header;
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
}
