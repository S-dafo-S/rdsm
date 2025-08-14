package com.radiant.fileAccess.service;

import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;

public interface FileDataConnectorVisitor<T> {
   T visit(LocalFileSystemDataConnector localFSDataConnector) throws Exception;

   T visit(MinioDataConnector minioDataConnector) throws Exception;
}
