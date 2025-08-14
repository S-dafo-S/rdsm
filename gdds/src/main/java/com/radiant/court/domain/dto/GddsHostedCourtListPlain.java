package com.radiant.court.domain.dto;

import java.util.List;

public class GddsHostedCourtListPlain {
   private List<GddsHostedCourtPlainDto> hostedCourtList;

   public List<GddsHostedCourtPlainDto> getHostedCourtList() {
      return this.hostedCourtList;
   }

   public void setHostedCourtList(final List<GddsHostedCourtPlainDto> hostedCourtList) {
      this.hostedCourtList = hostedCourtList;
   }
}
