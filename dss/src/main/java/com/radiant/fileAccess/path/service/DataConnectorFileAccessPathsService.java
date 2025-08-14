package com.radiant.fileAccess.path.service;

import com.radiant.fileAccess.path.dto.DataConnectorFilePathsDto;
import com.radiant.fileAccess.path.dto.FileAccessPathsBatchCreateRequest;
import java.util.List;

public interface DataConnectorFileAccessPathsService {
   List<DataConnectorFilePathsDto> getAll();

   DataConnectorFilePathsDto get(Long connectorId);

   DataConnectorFilePathsDto appendToConnectorPaths(FileAccessPathsBatchCreateRequest request);

   DataConnectorFilePathsDto overrideConnectorPaths(FileAccessPathsBatchCreateRequest request);

   DataConnectorFilePathsDto deleteConnectorPaths(Long connectorId);
}
