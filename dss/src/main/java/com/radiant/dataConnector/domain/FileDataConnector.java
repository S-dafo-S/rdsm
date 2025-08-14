package com.radiant.dataConnector.domain;

import com.radiant.fileAccess.service.FileDataConnectorVisitor;
import java.nio.file.Path;

public abstract class FileDataConnector extends DataConnector {
   public abstract String normalize(Path input);

   public abstract <T> T accept(FileDataConnectorVisitor<T> visitor) throws Exception;
}
