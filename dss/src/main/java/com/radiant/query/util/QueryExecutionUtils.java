package com.radiant.query.util;

import com.radiant.court.DataStore;
import com.radiant.court.domain.CourtDataStore;
import com.radiant.court.domain.DssCourt;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.utils.DataStoreUtils;
import com.radiant.exception.court.DataStoreNotConfigured;
import com.radiant.exception.query.MultipleQueryImplementationsException;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.service.SqlQueryArgumentResolver;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class QueryExecutionUtils {
   public static final int INDENT_FACTOR = 4;

   /** @deprecated */
   @Deprecated
   public static List<DataConnector> pickCourtLinkedDataConnectors(DssCourt court, DssQuery query) {
      List<DataStore> suitableDataStores = DataStoreUtils.getDataStoresByCaseType(query.getCaseType());
      List<DataConnector> connectors = (List)court.getHostedCourt().getDataStores().stream().filter((ds) -> ds.getDataConnector() != null && suitableDataStores.contains(ds.getDataStore())).map(CourtDataStore::getDataConnector).collect(Collectors.toList());
      if (connectors.isEmpty()) {
         throw new DataStoreNotConfigured();
      } else {
         return connectors;
      }
   }

   public static DssQueryImplementation pickActiveImplementation(DssQuery query, List<DataConnector> connectors) {
      Set<Long> dcIds = (Set)connectors.stream().map(DataConnector::getId).collect(Collectors.toSet());
      List<DssQueryImplementation> implementations = (List)query.getImplementations().stream().filter((impl) -> impl.getIsActive() && impl.getQueryImplDataConnectors().stream().anyMatch((namedDc) -> namedDc.getDataConnectors().stream().anyMatch((dc) -> dcIds.contains(dc.getId())))).collect(Collectors.toList());
      if (implementations.size() > 1) {
         throw new MultipleQueryImplementationsException(query.getName());
      } else {
         return implementations.isEmpty() ? null : (DssQueryImplementation)implementations.get(0);
      }
   }

   public static Map<String, String> prepareParams(Map<String, String> arguments, Map<String, String> params) {
      Map<String, String> extendedParams = new LinkedCaseInsensitiveMap();
      extendedParams.putAll(arguments);

      for(Map.Entry<String, String> entry : params.entrySet()) {
         if (!arguments.containsKey(entry.getKey())) {
            extendedParams.put(entry.getKey(), entry.getValue());
         }
      }

      return extendedParams;
   }

   public static String resolveSqlArguments(DssQuery query, String sqlExpression, Map<String, String> arguments, Set<String> additionalParamKeys) {
      SqlQueryArgumentResolver resolver = new SqlQueryArgumentResolver(query, arguments, additionalParamKeys);
      return resolver.execute(sqlExpression);
   }
}
