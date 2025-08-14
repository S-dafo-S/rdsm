package com.radiant.court.domain.repository;

import com.radiant.court.domain.DssCourt;
import com.radiant.court.domain.DssHostedCourt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DssHostedCourtRepository extends JpaRepository<DssHostedCourt, Long> {
   Optional<DssHostedCourt> findByCourt(DssCourt court);

   @Query(
      value = "SELECT * FROM court_host WHERE court = (SELECT id FROM court WHERE region = ?1 AND level = ?2) ",
      nativeQuery = true
   )
   DssHostedCourt getByRegionAndLevel(Long region, Long level);

   List<DssHostedCourt> findAllByCourtRegionIdAndCourtLevel(Long courtRegionId, Long courtLevel);
}
