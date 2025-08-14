package com.radiant.dataConnector.domain.repository;

import com.radiant.dataConnector.domain.DataConnectorType;
import com.radiant.dataConnector.domain.DbmsType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbmsTypeRepository extends JpaRepository<DbmsType, DataConnectorType> {
   Optional<DbmsType> findByType(DataConnectorType type);
}
