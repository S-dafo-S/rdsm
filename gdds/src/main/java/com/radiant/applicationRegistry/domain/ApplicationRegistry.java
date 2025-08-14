package com.radiant.applicationRegistry.domain;

import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "application_registry",
   uniqueConstraints = {@UniqueConstraint(
   name = "application_registry_app_id_unic",
   columnNames = {"app_id"}
)}
)
public class ApplicationRegistry implements AuditableEntity {
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
      name = "app_id",
      nullable = false
   )
   private String appId;
   @Column(
      name = "app_name",
      nullable = false
   )
   private String appName;
   @Column(
      name = "password",
      nullable = false
   )
   private String password;
   @Column(
      name = "session_lease_time"
   )
   private Integer sessionLeaseTime;
   @OneToMany(
      cascade = {CascadeType.ALL},
      mappedBy = "applicationRegistry",
      orphanRemoval = true,
      fetch = FetchType.EAGER
   )
   private List<IpAddress> ipAddresses = new ArrayList();
   @ElementCollection
   @CollectionTable(
      name = "application_registry_dnode_access",
      joinColumns = {@JoinColumn(
   name = "registry_id"
)},
      foreignKey = @ForeignKey(
   name = "application_registry_dnode_access_registry_fk"
)
   )
   @Column(
      name = "dnode_id",
      nullable = false
   )
   private List<Long> dnodeAccess = new ArrayList();
   @ElementCollection
   @CollectionTable(
      name = "application_registry_api_access",
      joinColumns = {@JoinColumn(
   name = "registry_id"
)},
      foreignKey = @ForeignKey(
   name = "application_registry_api_access_registry_fk"
)
   )
   @Column(
      name = "api_id",
      nullable = false
   )
   private List<Long> apiAccess = new ArrayList();

   public AuditObject toAuditObject() {
      return new AuditObject(AuditObjectType.APPLICATION_REGISTRY, this.id, this.appId);
   }

   public Long getId() {
      return this.id;
   }

   public String getAppId() {
      return this.appId;
   }

   public String getAppName() {
      return this.appName;
   }

   public String getPassword() {
      return this.password;
   }

   public Integer getSessionLeaseTime() {
      return this.sessionLeaseTime;
   }

   public List<IpAddress> getIpAddresses() {
      return this.ipAddresses;
   }

   public List<Long> getDnodeAccess() {
      return this.dnodeAccess;
   }

   public List<Long> getApiAccess() {
      return this.apiAccess;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setAppId(final String appId) {
      this.appId = appId;
   }

   public void setAppName(final String appName) {
      this.appName = appName;
   }

   public void setPassword(final String password) {
      this.password = password;
   }

   public void setSessionLeaseTime(final Integer sessionLeaseTime) {
      this.sessionLeaseTime = sessionLeaseTime;
   }

   public void setIpAddresses(final List<IpAddress> ipAddresses) {
      this.ipAddresses = ipAddresses;
   }

   public void setDnodeAccess(final List<Long> dnodeAccess) {
      this.dnodeAccess = dnodeAccess;
   }

   public void setApiAccess(final List<Long> apiAccess) {
      this.apiAccess = apiAccess;
   }
}
