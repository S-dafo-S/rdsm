package com.radiant.court.domain;

import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.region.domain.GddsRegion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GddsCourtHostRepository extends JpaRepository<GddsHostedCourt, Long> {
   Optional<GddsHostedCourt> findByDssAndCourtId(DNode dnode, Long courtId);

   @Transactional
   void deleteByDssAndCourtId(DNode dnode, Long courtId);

   List<GddsHostedCourt> findByCourt(GddsCourt court);

   List<GddsHostedCourt> findAllByCourt_ParentRegionRegion(GddsRegion regionId);
}
