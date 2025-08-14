package com.radiant.region.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionCourtRepository extends JpaRepository<RegionCourt, RegionCourt.CompositeKey> {
   @Query("FROM RegionCourt AS rc INNER JOIN FETCH rc.court")
   List<RegionCourt> getAllRegionCourts();

   List<RegionCourt> findByRegion(GddsRegion region);
}
