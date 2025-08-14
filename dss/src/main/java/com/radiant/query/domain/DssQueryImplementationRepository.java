package com.radiant.query.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DssQueryImplementationRepository extends JpaRepository<DssQueryImplementation, Long> {
   List<DssQueryImplementation> findByQueryId(Long queryId);

   Optional<DssQueryImplementation> findByIdAndQueryId(Long id, Long queryId);
}
