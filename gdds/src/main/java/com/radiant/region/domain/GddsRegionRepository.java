package com.radiant.region.domain;

import com.radiant.region.domain.dto.GddsRegionCourtPair;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GddsRegionRepository extends JpaRepository<GddsRegion, Long> {
   boolean existsByOutdatedIsTrue();

   List<GddsRegion> findByParentIdIn(Collection<Long> parentIds);

   List<GddsRegion> findByOutdatedIsTrue();

   @Query("SELECT new com.radiant.region.domain.dto.GddsRegionCourtPair(r, c, (CASE WHEN EXISTS(SELECT subregion FROM GddsRegion subregion WHERE subregion.parent = r) OR EXISTS (SELECT rc FROM RegionCourt rc JOIN rc.court as childrenCourt WHERE rc.region = r AND childrenCourt.level > r.level) THEN TRUE ELSE FALSE END)) FROM GddsRegion r LEFT JOIN r.parent as p LEFT JOIN r.childrenCourts as children LEFT JOIN GddsCourt c ON (children.court = c OR c IS NULL)WHERE (:levelLimit IS NULL OR (r.level <= :levelLimit AND c.level <= :levelLimit)) AND ((:regions) IS NULL OR r.id in (:regions) OR (p.id IN (:regions) AND r.level = c.level)) ORDER BY r.id")
   List<GddsRegionCourtPair> findRegionsWithTopCourts(Long levelLimit, List<Long> regions);

   boolean existsByParentIsNull();

   List<GddsRegion> findAllByParent(GddsRegion parent);
}
