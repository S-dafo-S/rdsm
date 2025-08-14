package com.radiant.integrationFunction.domain;

import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import com.radiant.query.domain.JavaQueryImplementation;
import com.radiant.query.registry.QueryPluginEntry;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
   name = "integration_function",
   uniqueConstraints = {@UniqueConstraint(
   name = "integration_function_name_uniq",
   columnNames = {"name"}
)}
)
public class IntegrationFunction implements AuditableEntity {
   public static final String NAME_UNIQ_CONSTRAINT = "integration_function_name_uniq";
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
      name = "name",
      nullable = false
   )
   private String name;
   @Column(
      name = "description"
   )
   private String description;
   @Column(
      name = "original_filename",
      nullable = false
   )
   private String originalFilename;
   @Column(
      name = "uploaded_filename",
      nullable = false
   )
   private String uploadedFilename;
   @ElementCollection
   @CollectionTable(
      name = "integration_function_connector",
      joinColumns = {@JoinColumn(
   name = "integration_function_id"
)},
      foreignKey = @ForeignKey(
   name = "integration_function_connector_func_fk"
),
      uniqueConstraints = {@UniqueConstraint(
   name = "integration_function_connector_key_uniq",
   columnNames = {"integration_function_id", "key"}
)}
   )
   private List<IntegrationConnector> connectors = new ArrayList();
   @ElementCollection
   @CollectionTable(
      name = "integration_function_param",
      joinColumns = {@JoinColumn(
   name = "integration_function_id"
)},
      foreignKey = @ForeignKey(
   name = "integration_function_param_func_fk"
)
   )
   @Column(
      name = "key",
      nullable = false
   )
   private List<String> parameters = new ArrayList();
   @Column(
      name = "creation_date"
   )
   @Temporal(TemporalType.TIMESTAMP)
   @CreationTimestamp
   private Date creationDate;
   @UpdateTimestamp
   @Temporal(TemporalType.TIMESTAMP)
   @Column(
      name = "update_date"
   )
   private Date updateDate;
   @OneToMany(
      mappedBy = "integrationFunction"
   )
   private List<JavaQueryImplementation> usedBy = new ArrayList();
   @ElementCollection
   @CollectionTable(
      name = "integration_function_entry",
      joinColumns = {@JoinColumn(
   name = "function_id"
)},
      foreignKey = @ForeignKey(
   name = "integration_function_entry_func_fk"
),
      uniqueConstraints = {@UniqueConstraint(
   name = "integration_function_entry_classname_uniq",
   columnNames = {"class_name"}
)}
   )
   private List<QueryPluginEntry> pluginEntries = new ArrayList();

   public IntegrationFunction(String name) {
      this.name = name;
   }

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
      return new AuditObject(AuditObjectType.INTEGRATION_FUNCTION, this.getId(), this.getName());
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getOriginalFilename() {
      return this.originalFilename;
   }

   public String getUploadedFilename() {
      return this.uploadedFilename;
   }

   public List<IntegrationConnector> getConnectors() {
      return this.connectors;
   }

   public List<String> getParameters() {
      return this.parameters;
   }

   public List<JavaQueryImplementation> getUsedBy() {
      return this.usedBy;
   }

   public List<QueryPluginEntry> getPluginEntries() {
      return this.pluginEntries;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public void setOriginalFilename(final String originalFilename) {
      this.originalFilename = originalFilename;
   }

   public void setUploadedFilename(final String uploadedFilename) {
      this.uploadedFilename = uploadedFilename;
   }

   public void setConnectors(final List<IntegrationConnector> connectors) {
      this.connectors = connectors;
   }

   public void setParameters(final List<String> parameters) {
      this.parameters = parameters;
   }

   public void setUsedBy(final List<JavaQueryImplementation> usedBy) {
      this.usedBy = usedBy;
   }

   public void setPluginEntries(final List<QueryPluginEntry> pluginEntries) {
      this.pluginEntries = pluginEntries;
   }

   public IntegrationFunction() {
   }
}
