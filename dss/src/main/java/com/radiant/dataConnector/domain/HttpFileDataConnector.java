package com.radiant.dataConnector.domain;

import com.radiant.fileAccess.service.HttpDataConnectorVisitor;

public abstract class HttpFileDataConnector extends FileDataConnector {
   public abstract <T> T accept(HttpDataConnectorVisitor<T> visitor) throws Exception;
}
