package com.radiant.query.domain;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GddsQueryRepository extends JpaRepository<GddsQuery, Long> {
   @Query("SELECT q FROM com.radiant.query.domain.GddsQuery q JOIN q.programEntries e WHERE (:qId IS NULL OR q.id <> :qId) AND e.className = :className")
   List<GddsQuery> getDuplicatesByClassName(@Nullable Long qId, String className);

   @Query("SELECT q FROM com.radiant.query.domain.GddsQuery q JOIN q.programEntries e WHERE (:qId IS NULL OR q.id <> :qId) AND e.serviceName = :serviceName")
   List<GddsQuery> getDuplicatesByServiceName(@Nullable Long qId, String serviceName);

   Optional<GddsQuery> findByNameIgnoreCase(String name);

   List<GddsQuery> getBySyncStatus(SyncStatus status);

   @Transactional
   void deleteByName(String name);
}
