package com.radiant.fileAccess.path.domain.repository;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAccessPathRepository extends JpaRepository<FileAccessPath, Long> {
   List<FileAccessPath> findAllByLogicalPathStartingWith(String logicalPath);

   List<FileAccessPath> findByConnectorInAndLogicalPathStartingWith(List<DataConnector> dataConnectors, String logicalPath);

   Optional<FileAccessPath> findByLogicalPath(String logicalPath);

   Optional<FileAccessPath> findByConnector(DataConnector dataConnector);
}
