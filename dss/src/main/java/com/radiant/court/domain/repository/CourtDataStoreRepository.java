package com.radiant.court.domain.repository;

import com.radiant.court.domain.CourtDataStore;
import com.radiant.dataConnector.domain.DataConnector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtDataStoreRepository extends JpaRepository<CourtDataStore, Long> {
   void deleteByDataConnector(DataConnector dataConnector);
}
