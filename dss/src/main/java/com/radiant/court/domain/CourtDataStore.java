package com.radiant.court.domain;

import com.radiant.court.DataStore;
import com.radiant.dataConnector.domain.DataConnector;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
   name = "court_data_store"
)
public class CourtDataStore {
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
      name = "court_host",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "court_data_store_court_host_fk"
)
   )
   private DssHostedCourt hostedCourt;
   @Enumerated(EnumType.STRING)
   @Column(
      name = "data_store",
      nullable = false
   )
   private DataStore dataStore;
   @ColumnDefault("false")
   @Column(
      name = "is_hosted",
      nullable = false
   )
   private Boolean isHosted = false;
   @ManyToOne
   @JoinColumn(
      name = "data_connector",
      foreignKey = @ForeignKey(
   name = "court_data_store_dc_fk"
)
   )
   private DataConnector dataConnector;

   public CourtDataStore(DssHostedCourt hostedCourt, DataStore dataStore, Boolean isHosted) {
      this.hostedCourt = hostedCourt;
      this.dataStore = dataStore;
      this.isHosted = isHosted;
   }

   public Long getId() {
      return this.id;
   }

   public DssHostedCourt getHostedCourt() {
      return this.hostedCourt;
   }

   public DataStore getDataStore() {
      return this.dataStore;
   }

   public Boolean getIsHosted() {
      return this.isHosted;
   }

   public DataConnector getDataConnector() {
      return this.dataConnector;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setHostedCourt(final DssHostedCourt hostedCourt) {
      this.hostedCourt = hostedCourt;
   }

   public void setDataStore(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   public void setIsHosted(final Boolean isHosted) {
      this.isHosted = isHosted;
   }

   public void setDataConnector(final DataConnector dataConnector) {
      this.dataConnector = dataConnector;
   }

   public CourtDataStore() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof CourtDataStore)) {
         return false;
      } else {
         CourtDataStore other = (CourtDataStore)o;
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

            Object this$hostedCourt = this.getHostedCourt();
            Object other$hostedCourt = other.getHostedCourt();
            if (this$hostedCourt == null) {
               if (other$hostedCourt != null) {
                  return false;
               }
            } else if (!this$hostedCourt.equals(other$hostedCourt)) {
               return false;
            }

            Object this$dataStore = this.getDataStore();
            Object other$dataStore = other.getDataStore();
            if (this$dataStore == null) {
               if (other$dataStore != null) {
                  return false;
               }
            } else if (!this$dataStore.equals(other$dataStore)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof CourtDataStore;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $hostedCourt = this.getHostedCourt();
      result = result * 59 + ($hostedCourt == null ? 43 : $hostedCourt.hashCode());
      Object $dataStore = this.getDataStore();
      result = result * 59 + ($dataStore == null ? 43 : $dataStore.hashCode());
      return result;
   }
}
