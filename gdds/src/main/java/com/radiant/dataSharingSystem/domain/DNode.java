package com.radiant.dataSharingSystem.domain;

import com.radiant.court.domain.GddsCourt;
import com.radiant.ecrypt.EncryptorConverter;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import com.radiant.util.DateUtils;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
   name = "dnode",
   uniqueConstraints = {@UniqueConstraint(
   name = "dnode_name_uniq",
   columnNames = {"name"}
), @UniqueConstraint(
   name = "dnode_account_id_uniq",
   columnNames = {"account_id"}
)}
)
public class DNode implements AuditableEntity {
   public static final String NAME_UNIQ_CONSTRAINT = "dnode_name_uniq";
   public static final String ACCOUNT_ID_UNIQ_CONSTRAINT = "dnode_account_id_uniq";
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   @Column(
      name = "id",
      updatable = false,
      nullable = false
   )
   private Long id;
   @Column(
      name = "name",
      nullable = false
   )
   private String name;
   @ManyToOne
   @JoinColumn(
      name = "deploy_court",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "dnode_deploy_court_fk"
)
   )
   private GddsCourt deployCourt;
   @Column(
      name = "account_id",
      nullable = false
   )
   private String accountId;
   @Convert(
      converter = EncryptorConverter.class
   )
   @Column(
      name = "account_password",
      nullable = false
   )
   private String accountPassword;
   @CreationTimestamp
   @Temporal(TemporalType.TIMESTAMP)
   @Column(
      name = "creation_date"
   )
   private Date creationDate;
   @UpdateTimestamp
   @Temporal(TemporalType.TIMESTAMP)
   @Column(
      name = "update_date"
   )
   private Date updateDate;
   @Column(
      name = "dnode_url"
   )
   private String dnodeUrl;
   @Column(
      name = "token",
      length = 1024
   )
   private String token;
   @Column(
      name = "qnode_token",
      length = 1024
   )
   private String qnodeToken;
   @Column(
      name = "version"
   )
   private String version;
   @Column(
      name = "connect_status"
   )
   private Boolean connectStatus;
   @Column(
      name = "access_all"
   )
   private Long accessAll;
   @Column(
      name = "access_fail"
   )
   private Long accessFail;

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public void setCreationDate(Date creationDate) {
      this.creationDate = DateUtils.cloneDate(creationDate);
   }

   public Date getUpdateDate() {
      return DateUtils.cloneDate(this.updateDate);
   }

   public void setUpdateDate(Date updateDate) {
      this.updateDate = DateUtils.cloneDate(updateDate);
   }

   public AuditObject toAuditObject() {
      return new AuditObject(AuditObjectType.DATA_SHARING_SYSTEM, this.id, this.name);
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public GddsCourt getDeployCourt() {
      return this.deployCourt;
   }

   public String getAccountId() {
      return this.accountId;
   }

   public String getAccountPassword() {
      return this.accountPassword;
   }

   public String getDnodeUrl() {
      return this.dnodeUrl;
   }

   public String getToken() {
      return this.token;
   }

   public String getQnodeToken() {
      return this.qnodeToken;
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

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setDeployCourt(final GddsCourt deployCourt) {
      this.deployCourt = deployCourt;
   }

   public void setAccountId(final String accountId) {
      this.accountId = accountId;
   }

   public void setAccountPassword(final String accountPassword) {
      this.accountPassword = accountPassword;
   }

   public void setDnodeUrl(final String dnodeUrl) {
      this.dnodeUrl = dnodeUrl;
   }

   public void setToken(final String token) {
      this.token = token;
   }

   public void setQnodeToken(final String qnodeToken) {
      this.qnodeToken = qnodeToken;
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
}
