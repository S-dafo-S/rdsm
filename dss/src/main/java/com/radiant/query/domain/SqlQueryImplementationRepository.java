package com.radiant.query.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlQueryImplementationRepository extends JpaRepository<SqlQueryImplementation, Long> {
}
