package com.radiant.fileAccess.path.service;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.dto.FileAccessApiDto;
import com.radiant.fileAccess.path.dto.FileAccessPathCreateRequest;
import com.radiant.fileAccess.path.dto.FileAccessPathDto;
import java.nio.file.Path;
import java.util.List;

public interface FileAccessPathService {
   FileAccessPathDto get(Long id);

   FileAccessApiDto findByLogicalPath(String logicalPath);

   List<FileAccessPathDto> getAll();

   FileAccessPathDto create(FileAccessPathCreateRequest request);

   FileAccessPathDto update(Long id, FileAccessPathCreateRequest request);

   void delete(Long id);

   FileAccessPath findByLogicalPath(Path path);

   FileAccessPath findByLogicalPathAndConnector(Path path, List<DataConnector> dataConnectors);
}
