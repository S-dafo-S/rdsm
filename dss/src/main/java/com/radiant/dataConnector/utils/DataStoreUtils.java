package com.radiant.dataConnector.utils;

import com.radiant.CaseType;
import com.radiant.court.DataStore;
import com.radiant.court.domain.CourtDataStore;
import com.radiant.court.domain.DssHostedCourt;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

public class DataStoreUtils {
   public static DataStore pickByTypeAndKind(@NotNull CaseType type, @NotNull DataConnectorKind kind) {
      if (type == CaseType.LIVE) {
         return kind == DataConnectorKind.DB ? DataStore.LIVE_DB : DataStore.LIVE_FS;
      } else if (type == CaseType.ARCHIVED) {
         return kind == DataConnectorKind.DB ? DataStore.ARCHIVE_DB : DataStore.ARCHIVE_FS;
      } else {
         throw new IllegalArgumentException("Unknown data store type, case type: " + type + ", connector kind: " + kind);
      }
   }

   public static List<DssHostedCourt> getAssociatedCourtsByDataStore(DataStore dataStore, DataConnector dataConnector) {
      return dataConnector.getCourtDataStores().stream().filter((store) -> store.getDataStore() == dataStore).map(CourtDataStore::getHostedCourt).collect(Collectors.toList());
   }

   public static List<DataStore> getDataStoresByCaseType(@NotNull CaseType type) {
      return type == CaseType.LIVE ? Arrays.asList(DataStore.LIVE_DB, DataStore.LIVE_FS) : Arrays.asList(DataStore.ARCHIVE_DB, DataStore.ARCHIVE_FS);
   }

   public static boolean isDoc(DataStore dataStore) {
      return dataStore == DataStore.LIVE_FS || dataStore == DataStore.ARCHIVE_FS;
   }
}
