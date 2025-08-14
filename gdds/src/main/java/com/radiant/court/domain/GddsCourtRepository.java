package com.radiant.court.domain;

import com.radiant.region.domain.GddsRegion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface GddsCourtRepository extends JpaRepository<GddsCourt, Long> {
   boolean existsByParentRegionRegionAndLevel(GddsRegion region, Long level);

   List<GddsCourt> findByParentRegionRegionAndLevel(GddsRegion region, Long level);

   Optional<GddsCourt> findByName(String name);

   @Query("SELECT c FROM GddsCourt c WHERE c.name like CONCAT('%', :name, '%')")
   List<GddsCourt> findByPartialName(@NonNull String name);
}
