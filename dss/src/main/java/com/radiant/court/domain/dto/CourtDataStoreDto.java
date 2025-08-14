package com.radiant.court.domain.dto;

import com.radiant.court.DataStore;
import com.radiant.court.domain.CourtDataStore;
import javax.validation.constraints.NotNull;

public class CourtDataStoreDto {
   private @NotNull Long hostedCourtId;
   private @NotNull Boolean isHosted = false;
   private @NotNull DataStore dataStore;
   private Long dataConnector;

   public CourtDataStoreDto(CourtDataStore courtDataStore) {
      this.hostedCourtId = courtDataStore.getHostedCourt().getId();
      this.isHosted = courtDataStore.getIsHosted();
      this.dataStore = courtDataStore.getDataStore();
      if (courtDataStore.getDataConnector() != null) {
         this.dataConnector = courtDataStore.getDataConnector().getId();
      }

   }

   public Long getHostedCourtId() {
      return this.hostedCourtId;
   }

   public Boolean getIsHosted() {
      return this.isHosted;
   }

   public DataStore getDataStore() {
      return this.dataStore;
   }

   public Long getDataConnector() {
      return this.dataConnector;
   }

   public void setHostedCourtId(final Long hostedCourtId) {
      this.hostedCourtId = hostedCourtId;
   }

   public void setIsHosted(final Boolean isHosted) {
      this.isHosted = isHosted;
   }

   public void setDataStore(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   public void setDataConnector(final Long dataConnector) {
      this.dataConnector = dataConnector;
   }

   public CourtDataStoreDto() {
   }
}
