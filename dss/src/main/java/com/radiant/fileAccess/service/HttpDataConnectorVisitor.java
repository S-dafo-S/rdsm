package com.radiant.fileAccess.service;

import com.radiant.dataConnector.domain.MinioDataConnector;

public interface HttpDataConnectorVisitor<T> {
   T visit(MinioDataConnector minioDataConnector) throws Exception;
}
