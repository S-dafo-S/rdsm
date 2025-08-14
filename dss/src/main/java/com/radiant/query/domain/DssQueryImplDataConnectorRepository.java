package com.radiant.query.domain;

import com.radiant.dataConnector.domain.DataConnector;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DssQueryImplDataConnectorRepository extends JpaRepository<DssQueryImplDataConnector, Long> {
   boolean existsByDataConnectorsIn(List<DataConnector> dataConnectors);
}
