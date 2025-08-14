package com.radiant.query.domain;

import com.radiant.query.domain.dto.DssQueryDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DssQueryRepository extends JpaRepository<DssQuery, Long> {
   @Query("SELECT new com.radiant.query.domain.dto.DssQueryDto(q, COUNT(DISTINCT impl)) FROM DssQuery q LEFT JOIN DssQueryImplementation impl ON impl.query = q GROUP BY q.id")
   List<DssQueryDto> findAllWithStatistic();

   Optional<DssQuery> findByNameIgnoreCase(String name);
}
