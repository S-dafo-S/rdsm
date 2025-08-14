package com.radiant.court.service;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.region.domain.GddsRegion;

public class GddsCourtUtils {
   public static CourtTreeNode createLeafNode(GddsCourt court) {
      CourtTreeNode leaf = new CourtTreeNode();
      leaf.setId(court.getId());
      leaf.setName(court.getName());
      leaf.setLevel(court.getLevel());
      leaf.setIsContainer(false);
      leaf.setShortName(court.getShortName());
      return leaf;
   }

   public static CourtTreeNode createLeafNode(GddsRegion region) {
      CourtTreeNode leaf = new CourtTreeNode();
      leaf.setId(region.getId());
      leaf.setName(region.getName());
      leaf.setLevel(region.getLevel());
      leaf.setIsContainer(true);
      leaf.setShortName(region.getShortName());
      return leaf;
   }
}
