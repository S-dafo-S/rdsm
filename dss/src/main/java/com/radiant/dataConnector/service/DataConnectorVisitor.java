package com.radiant.dataConnector.service;

import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;

public interface DataConnectorVisitor<T> {
   T visit(JdbcDataConnector jdbcDataConnector);

   T visit(LocalFileSystemDataConnector localFSDataConnector);

   T visit(MinioDataConnector minioDataConnector);
}
