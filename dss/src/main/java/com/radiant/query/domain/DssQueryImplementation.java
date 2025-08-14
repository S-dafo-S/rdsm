package com.radiant.query.domain;

import com.radiant.query.domain.dto.QueryLanguage;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
   name = "query_implementation",
   uniqueConstraints = {@UniqueConstraint(
   name = "query_impl_name_uniq",
   columnNames = {"name", "query"}
)}
)
@Inheritance(
   strategy = InheritanceType.JOINED
)
public abstract class DssQueryImplementation {
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
   @ManyToOne(
      fetch = FetchType.LAZY
   )
   @JoinColumn(
      name = "query",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "query_implementation_query_fk"
)
   )
   private DssQuery query;
   @OneToMany(
      mappedBy = "queryImplementation",
      cascade = {CascadeType.ALL},
      orphanRemoval = true
   )
   private List<DssQueryImplDataConnector> queryImplDataConnectors = new ArrayList();
   @ColumnDefault("true")
   @Column(
      name = "active",
      nullable = false
   )
   private Boolean isActive = true;
   @Temporal(TemporalType.TIMESTAMP)
   @Column(
      name = "activation_date"
   )
   private Date activationDate;
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
   @ElementCollection
   @CollectionTable(
      name = "query_implementation_param",
      joinColumns = {@JoinColumn(
   name = "query_impl_id"
)},
      foreignKey = @ForeignKey(
   name = "query_implementation_param_query_impl_fk"
)
   )
   @MapKeyColumn(
      name = "name"
   )
   @Column(
      name = "value",
      nullable = false,
      columnDefinition = "TEXT"
   )
   private Map<String, String> parameters = new HashMap();

   public DssQueryImplementation(String name, DssQuery query, Boolean isActive) {
      this.name = name;
      this.query = query;
      this.isActive = isActive;
      if (isActive) {
         this.activationDate = new Date();
      }

   }

   public Date getActivationDate() {
      return DateUtils.cloneDate(this.activationDate);
   }

   public void setActivationDate(Date activationDate) {
      this.activationDate = DateUtils.cloneDate(activationDate);
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

   public abstract QueryLanguage getLanguage();

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public DssQuery getQuery() {
      return this.query;
   }

   public List<DssQueryImplDataConnector> getQueryImplDataConnectors() {
      return this.queryImplDataConnectors;
   }

   public Boolean getIsActive() {
      return this.isActive;
   }

   public Map<String, String> getParameters() {
      return this.parameters;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setQuery(final DssQuery query) {
      this.query = query;
   }

   public void setQueryImplDataConnectors(final List<DssQueryImplDataConnector> queryImplDataConnectors) {
      this.queryImplDataConnectors = queryImplDataConnectors;
   }

   public void setIsActive(final Boolean isActive) {
      this.isActive = isActive;
   }

   public void setParameters(final Map<String, String> parameters) {
      this.parameters = parameters;
   }

   public DssQueryImplementation() {
   }
}
