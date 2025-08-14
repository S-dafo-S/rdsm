package com.radiant.court.service;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.GddsHostedCourt;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.kafka.DssCourtHostEvent;
import com.radiant.region.domain.GddsRegion;
import java.util.List;

public interface GddsCourtHostService {
   List<GddsHostedCourt> getByCourt(GddsCourt court);

   CourtTreeNode getCourtTree(GddsRegion region);

   List<GddsHostedCourt> getAll();

   void processCourtHostUpdateEvent(DssCourtHostEvent event);
}
