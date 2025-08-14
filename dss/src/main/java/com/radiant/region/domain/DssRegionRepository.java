package com.radiant.region.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DssRegionRepository extends JpaRepository<DssRegion, Long> {
   Optional<DssRegion> findById(Long id);

   List<DssRegion> findByLevelGreaterThanEqual(Long level);

   List<DssRegion> findByParentOrderById(DssRegion parent);
}
