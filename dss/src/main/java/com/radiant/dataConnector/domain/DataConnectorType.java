package com.radiant.dataConnector.domain;

import java.util.function.Supplier;

public enum DataConnectorType {
   POSTGRESQL(JdbcDataConnector::new),
   MYSQL(JdbcDataConnector::new),
   DAMENG(JdbcDataConnector::new),
   LOCAL_FILE_SYSTEM(LocalFileSystemDataConnector::new),
   MINIO(MinioDataConnector::new);

   private final Supplier<DataConnector> makeNew;

   private DataConnectorType(Supplier<DataConnector> makeNew) {
      this.makeNew = makeNew;
   }

   public DataConnector makeNewConnector() {
      return (DataConnector)this.makeNew.get();
   }
}
