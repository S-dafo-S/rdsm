package com.radiant.dataConnector.domain.repository;

import com.radiant.dataConnector.domain.DataConnector;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataConnectorRepository extends JpaRepository<DataConnector, Long> {
   Optional<DataConnector> findByIdAndArchivedIsFalse(Long id);

   List<DataConnector> findByIdInAndArchivedIsFalse(Collection<Long> ids);

   List<DataConnector> findAllByArchivedIsFalse();

   Boolean existsByNameAndArchivedIsFalse(String name);

   Boolean existsByNameAndArchivedIsFalseAndIdNot(String name, Long id);

   DataConnector getByNameAndArchivedIsFalse(String name);
}
