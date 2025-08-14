package com.radiant.dataConnector.domain;

import com.radiant.fileAccess.service.S3TestConnectionVisitor;

public interface S3DataConnector {
   <T> boolean testConnection(S3TestConnectionVisitor<T> visitor) throws Exception;
}
