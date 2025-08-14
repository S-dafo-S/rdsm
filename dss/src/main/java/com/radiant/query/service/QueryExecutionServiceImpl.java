package com.radiant.query.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.config.QueryPluginProvider;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.dto.ExecuteQueryRequest;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.dto.StringValue;
import com.radiant.exception.NotImplementedException;
import com.radiant.exception.query.DssQueryIsNotImplementedException;
import com.radiant.exception.query.JavaQueryImplProgramNotFound;
import com.radiant.exception.query.NoSuchQueryException;
import com.radiant.exception.query.PaginationIsNotAvailableException;
import com.radiant.exception.query.QueryNotFoundByName;
import com.radiant.plugin.FileAccessAdapter;
import com.radiant.plugin.GlobalVariables;
import com.radiant.plugin.QueryAdapter;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplDataConnector;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.DssQueryRepository;
import com.radiant.query.domain.JavaQueryImplementation;
import com.radiant.query.domain.SqlQueryImplementation;
import com.radiant.query.domain.dto.QueryImplConnectorDto;
import com.radiant.query.domain.dto.QueryParamDto;
import com.radiant.query.domain.dto.QueryTestExecutionDto;
import com.radiant.query.util.PaginationUtil;
import com.radiant.query.util.QueryExecutionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class QueryExecutionServiceImpl implements QueryExecutionService {
   private static final Logger LOG = LoggerFactory.getLogger(QueryExecutionServiceImpl.class);
   private static final String PAGE_SIZE_PARAM = "pageSize";
   private static final String PAGE_NUM_PARAM = "pageNum";
   private static final int DEFAULT_PAGE_SIZE = 10;
   private static final int DEFAULT_PAGE_NUM = 1;
   @Autowired
   private DssQueryRepository dssQueryRepository;
   @Autowired
   private DataConnectorService dataConnectorService;
   @Autowired
   private QueryPluginProvider queryPluginProvider;

   public Object execute(String queryName, Map<String, String> arguments, @Nullable HttpServletResponse servletResponse) {
      this.getQuery(queryName);
      List<DataConnector> dataConnectors = this.dataConnectorService.getAllConnectors();
      DssQueryImplementation implementation = this.getQueryImplementation(queryName);
      if (implementation == null) {
         throw new DssQueryIsNotImplementedException(queryName);
      } else {
         return this.executeImpl(implementation, dataConnectors, arguments, servletResponse);
      }
   }

   public String testExecution(Long queryId, QueryTestExecutionDto testExecutionDto) {
      DssQuery query = this.getQuery(queryId);
      if (!testExecutionDto.getNamedConnectors().isEmpty() && !((QueryImplConnectorDto)testExecutionDto.getNamedConnectors().get(0)).getDataConnectors().isEmpty()) {
         List<DataConnector> courtLinkedConnectors = this.dataConnectorService.getAllConnectors();
         switch (testExecutionDto.getLang()) {
            case JAVA:
               throw new NotImplementedException();
            case SQL:
               Set<Long> executeDcId = (Set)courtLinkedConnectors.stream().map(DataConnector::getId).collect(Collectors.toSet());
               List<QueryImplConnectorDto> executionNamedDcList = (List)testExecutionDto.getNamedConnectors().stream().filter((namedDc) -> {
                  Stream<Long> dcIdStream = namedDc.getDataConnectors().stream();
                  return dcIdStream.anyMatch(executeDcId::contains);
               }).collect(Collectors.toList());
               if (executionNamedDcList.isEmpty()) {
                  throw new IllegalStateException("Query impl data connectors don't match to court data connectors");
               } else if (executionNamedDcList.size() > 1) {
                  throw new IllegalStateException("SQL implementation doesn't support multiple data connectors");
               } else {
                  QueryImplConnectorDto executionNamedDc = (QueryImplConnectorDto)executionNamedDcList.get(0);
                  if (executionNamedDc.getDataConnectors().size() > 1) {
                     throw new IllegalStateException("SQL implementation doesn't support multiple data connectors");
                  }

                  DataConnector dataConnector = this.dataConnectorService.getDataConnector((Long)executionNamedDc.getDataConnectors().get(0));
                  Map<String, String> extendedParams = QueryExecutionUtils.prepareParams(testExecutionDto.getTestArguments(), (Map)testExecutionDto.getParameters().stream().collect(Collectors.toMap(QueryParamDto::getKey, QueryParamDto::getValue)));
                  String expression = QueryExecutionUtils.resolveSqlArguments(query, testExecutionDto.getCode(), extendedParams, (Set)testExecutionDto.getParameters().stream().map(QueryParamDto::getKey).collect(Collectors.toSet()));
                  return this.dataConnectorService.executeQuery(dataConnector, new ExecuteQueryRequest(expression)).toString(4);
               }
            default:
               throw new IllegalStateException("Unexpected query language: " + testExecutionDto.getLang());
         }
      } else {
         throw new RuntimeException("Data connector must not be empty");
      }
   }

   public DssQueryImplementation getQueryImplementation(String queryName) {
      DssQuery query = this.getQuery(queryName);
      List<DataConnector> dataConnectors = this.dataConnectorService.getAllConnectors();
      return QueryExecutionUtils.pickActiveImplementation(query, dataConnectors);
   }

   public DssQueryImplementation getQueryImplementation(String queryName, DataConnector dataConnector) {
      DssQuery query = this.getQuery(queryName);
      return QueryExecutionUtils.pickActiveImplementation(query, Collections.singletonList(dataConnector));
   }

   public Object executeImpl(DssQueryImplementation implementation, List<DataConnector> dataConnectors, Map<String, String> arguments, @Nullable HttpServletResponse servletResponse) {
      String queryName = implementation.getQuery().getName();
      Map<String, String> extendedParams = QueryExecutionUtils.prepareParams(arguments, implementation.getParameters());
      if (implementation instanceof SqlQueryImplementation) {
         Set<Long> executeDcId = (Set)dataConnectors.stream().map(DataConnector::getId).collect(Collectors.toSet());
         List<DssQueryImplDataConnector> executionNamedDcList = (List)implementation.getQueryImplDataConnectors().stream().filter((namedDc) -> namedDc.getDataConnectors().stream().anyMatch((dc) -> executeDcId.contains(dc.getId()))).collect(Collectors.toList());
         if (executionNamedDcList.isEmpty()) {
            throw new IllegalStateException("Query impl data connectors don't match to court data connectors");
         } else if (executionNamedDcList.size() > 1) {
            throw new IllegalStateException("SQL implementation doesn't support multiple data connectors");
         } else {
            DssQueryImplDataConnector executionNamedDc = (DssQueryImplDataConnector)executionNamedDcList.get(0);
            if (executionNamedDc.getDataConnectors().size() > 1) {
               throw new IllegalStateException("SQL implementation doesn't support multiple data connectors");
            } else {
               DataConnector connector = (DataConnector)executionNamedDc.getDataConnectors().get(0);
               String expression = this.resolveSqlArguments((SqlQueryImplementation)implementation, extendedParams, implementation.getParameters().keySet());
               if (!extendedParams.containsKey("pageSize") && !extendedParams.containsKey("pageNum")) {
                  return this.dataConnectorService.executeQuery(connector, new ExecuteQueryRequest(expression)).toString(4);
               } else {
                  Integer pageSizeInt = this.getNullSafeParam(extendedParams, "pageSize", 10);
                  Integer pageNumInt = this.getNullSafeParam(extendedParams, "pageNum", 1);
                  return this.dataConnectorService.executeQuery(connector, new ExecuteQueryRequest(expression), pageSizeInt, pageNumInt).toString(4);
               }
            }
         }
      } else if (!(implementation instanceof JavaQueryImplementation)) {
         throw new RuntimeException("Unknown query impl language");
      } else {
         String pluginName = this.getExpectedPluginName(queryName, ((JavaQueryImplementation)implementation).getImplFilename());
         if (!this.queryPluginProvider.isExist(pluginName)) {
            List<Map.Entry<String, QueryAdapter>> queryPlugins = this.queryPluginProvider.getPluginsByNamePrefix(queryName);
            List<Map.Entry<String, FileAccessAdapter>> filAccessPlugins = this.queryPluginProvider.getFileAccessPluginsByNamePrefix(queryName);
            if (queryPlugins.size() == 1 && filAccessPlugins.isEmpty()) {
               pluginName = (String)((Map.Entry)queryPlugins.get(0)).getKey();
            } else {
               if (!queryPlugins.isEmpty() || filAccessPlugins.size() != 1) {
                  throw new JavaQueryImplProgramNotFound(implementation.getName(), queryName);
               }

               pluginName = (String)((Map.Entry)filAccessPlugins.get(0)).getKey();
            }
         }

         Object queryResult = this.queryPluginProvider.execute(pluginName, this.resolveGlobalVariables(implementation.getQuery()), extendedParams, servletResponse);
         if (!extendedParams.containsKey("pageSize") && !extendedParams.containsKey("pageNum")) {
            return queryResult != null ? this.wrapPlainString(queryResult.toString()) : "";
         } else {
            Integer pageSizeInt = this.getNullSafeParam(extendedParams, "pageSize", 10);
            Integer pageNumInt = this.getNullSafeParam(extendedParams, "pageNum", 1);
            JSONObject pagingWrapping = new JSONObject();

            JSONArray resultArray;
            try {
               resultArray = new JSONArray(queryResult);
            } catch (JSONException var14) {
               throw new PaginationIsNotAvailableException();
            }

            pagingWrapping.put("page", pageNumInt);
            pagingWrapping.put("pageSize", pageSizeInt);
            pagingWrapping.put("total", resultArray.length());
            pagingWrapping.put("data", PaginationUtil.getPage(resultArray.toList(), pageSizeInt, pageNumInt));
            return pagingWrapping.toString(4);
         }
      }
   }

   private String resolveSqlArguments(SqlQueryImplementation implementation, Map<String, String> arguments, Set<String> additionalParamKeys) {
      return QueryExecutionUtils.resolveSqlArguments(implementation.getQuery(), implementation.getCode(), arguments, additionalParamKeys);
   }

   private GlobalVariables resolveGlobalVariables(DssQuery query) {
      JavaQueryArgumentResolver resolver = new JavaQueryArgumentResolver(query);
      return resolver.resolveGlobalVariables();
   }

   private Integer getNullSafeParam(Map<String, String> params, String key, Integer defaultValue) {
      Integer result = null;
      if (params.containsKey(key)) {
         result = Integer.parseInt((String)params.get(key));
      }

      return result != null && result >= 1 ? result : defaultValue;
   }

   private String wrapPlainString(String input) {
      try {
         new JSONObject(input);
      } catch (JSONException var7) {
         try {
            new JSONArray(input);
         } catch (JSONException var6) {
            try {
               return (new ObjectMapper()).writeValueAsString(new StringValue(input));
            } catch (JsonProcessingException var5) {
               throw new RuntimeException("Failed to read query result");
            }
         }
      }

      return input;
   }

   private DssQuery getQuery(String name) {
      return (DssQuery)this.dssQueryRepository.findByNameIgnoreCase(name).orElseThrow(() -> new QueryNotFoundByName(name));
   }

   private DssQuery getQuery(Long id) {
      return (DssQuery)this.dssQueryRepository.findById(id).orElseThrow(() -> new NoSuchQueryException(id));
   }

   private String getExpectedPluginName(String queryName, String jarName) {
      if (jarName == null) {
         return queryName;
      } else {
         int extensionIndex = jarName.lastIndexOf(".jar");
         if (extensionIndex < 0) {
            LOG.warn("Unexpected query plugin jar name {}", jarName);
            return queryName;
         } else {
            return queryName.concat("_").concat(jarName.substring(0, extensionIndex));
         }
      }
   }
}
