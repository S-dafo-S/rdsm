package com.radiant.court.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class DssHostedCourtBatchRequest {
   List<DssHostedCourtDto> hostedCourts = new ArrayList();
   private List<CourtDataStoreDto> courtDataStores = new ArrayList();

   public List<DssHostedCourtDto> getHostedCourts() {
      return this.hostedCourts;
   }

   public List<CourtDataStoreDto> getCourtDataStores() {
      return this.courtDataStores;
   }

   public void setHostedCourts(final List<DssHostedCourtDto> hostedCourts) {
      this.hostedCourts = hostedCourts;
   }

   public void setCourtDataStores(final List<CourtDataStoreDto> courtDataStores) {
      this.courtDataStores = courtDataStores;
   }
}
