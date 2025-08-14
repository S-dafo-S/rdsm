package com.radiant.query.domain;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
   name = "query_implementation_data_connector"
)
public class DssQueryImplDataConnector {
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
   @ManyToOne
   @JoinColumn(
      name = "query_impl",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "query_implementation_data_connector_query_impl_fk"
)
   )
   private DssQueryImplementation queryImplementation;
   @Column(
      name = "key",
      nullable = false
   )
   private String key;
   @Enumerated(EnumType.STRING)
   @Column(
      name = "kind",
      nullable = false
   )
   private DataConnectorKind kind;
   @ManyToMany
   @JoinTable(
      name = "query_implementation_data_connector_dc",
      joinColumns = {@JoinColumn(
   name = "query_implementation_data_connector_id",
   foreignKey = @ForeignKey(
   name = "query_implementation_data_connector_dc_impl_fk"
)
)},
      inverseJoinColumns = {@JoinColumn(
   name = "data_connector_id",
   foreignKey = @ForeignKey(
   name = "query_implementation_data_connector_dc_connector_fk"
)
)}
   )
   private List<DataConnector> dataConnectors = new ArrayList();

   public DssQueryImplDataConnector(DssQueryImplementation queryImplementation, String key, DataConnectorKind kind) {
      this.queryImplementation = queryImplementation;
      this.key = key;
      this.kind = kind;
   }

   public Long getId() {
      return this.id;
   }

   public DssQueryImplementation getQueryImplementation() {
      return this.queryImplementation;
   }

   public String getKey() {
      return this.key;
   }

   public DataConnectorKind getKind() {
      return this.kind;
   }

   public List<DataConnector> getDataConnectors() {
      return this.dataConnectors;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setQueryImplementation(final DssQueryImplementation queryImplementation) {
      this.queryImplementation = queryImplementation;
   }

   public void setKey(final String key) {
      this.key = key;
   }

   public void setKind(final DataConnectorKind kind) {
      this.kind = kind;
   }

   public void setDataConnectors(final List<DataConnector> dataConnectors) {
      this.dataConnectors = dataConnectors;
   }

   public DssQueryImplDataConnector() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DssQueryImplDataConnector)) {
         return false;
      } else {
         DssQueryImplDataConnector other = (DssQueryImplDataConnector)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$id = this.getId();
            Object other$id = other.getId();
            if (this$id == null) {
               if (other$id != null) {
                  return false;
               }
            } else if (!this$id.equals(other$id)) {
               return false;
            }

            Object this$queryImplementation = this.getQueryImplementation();
            Object other$queryImplementation = other.getQueryImplementation();
            if (this$queryImplementation == null) {
               if (other$queryImplementation != null) {
                  return false;
               }
            } else if (!this$queryImplementation.equals(other$queryImplementation)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DssQueryImplDataConnector;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $queryImplementation = this.getQueryImplementation();
      result = result * 59 + ($queryImplementation == null ? 43 : $queryImplementation.hashCode());
      return result;
   }
}
