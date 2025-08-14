package com.radiant.court.domain.dto;

import java.util.List;

public class GddsCourtListPlain {
   private List<GddsCourtPlainDto> courtList;

   public List<GddsCourtPlainDto> getCourtList() {
      return this.courtList;
   }

   public void setCourtList(final List<GddsCourtPlainDto> courtList) {
      this.courtList = courtList;
   }
}
