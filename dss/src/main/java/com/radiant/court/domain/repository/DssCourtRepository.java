package com.radiant.court.domain.repository;

import com.radiant.court.domain.DssCourt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DssCourtRepository extends JpaRepository<DssCourt, Long> {
   Boolean existsByRegionIsNull();

   Optional<DssCourt> findByName(String name);
}
