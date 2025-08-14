package com.radiant.dataConnector.service;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.dataConnector.domain.dto.ExecuteQueryRequest;
import com.radiant.exception.query.DssQueryIsNotImplementedException;
import com.radiant.exception.query.QueryNotFoundByName;
import com.radiant.fileAccess.FileAccessUtils;
import com.radiant.plugin.FileAccessAdapter;
import com.radiant.plugin.GlobalVariables;
import com.radiant.plugin.QueryAdapter;
import com.radiant.plugin.dto.DataConnectorBaseInfo;
import com.radiant.plugin.dto.NamedDataConnectorResponse;
import com.radiant.plugin.service.PluginDataConnectorsHelper;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplDataConnector;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.DssQueryRepository;
import com.radiant.query.util.QueryExecutionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginDataConnectorsHelperImpl implements PluginDataConnectorsHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PluginDataConnectorsHelperImpl.class);
    @Autowired
    private DssQueryRepository dssQueryRepository;
    @Autowired
    private DataConnectorService dataConnectorService;

    public List<String> getAllDataConnectorKeys(QueryAdapter plugin, GlobalVariables globalVariables) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getAllDataConnectorKeys(query);
    }

    public List<String> getAllDataConnectorKeys(FileAccessAdapter plugin, GlobalVariables globalVariables) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getAllDataConnectorKeys(query);
    }

    public NamedDataConnectorResponse getDataConnectors(QueryAdapter plugin, GlobalVariables globalVariables, String key) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getNamedDataConnectors(query, key);
    }

    public NamedDataConnectorResponse getDataConnectors(FileAccessAdapter plugin, GlobalVariables globalVariables, String key) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getNamedDataConnectors(query, key);
    }

    public String getDataConnectorType(QueryAdapter plugin, GlobalVariables globalVariables, String key) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getDataConnectorType(query, key);
    }

    public String getDataConnectorType(FileAccessAdapter plugin, GlobalVariables globalVariables, String key) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.getDataConnectorType(query, key);
    }

    public Object executeSql(QueryAdapter plugin, GlobalVariables globalVariables, Long dataConnectorId, String sqlExpression, Map<String, String> arguments) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.executeSql(query, globalVariables, dataConnectorId, sqlExpression, arguments);
    }

    public Object executeSql(FileAccessAdapter plugin, GlobalVariables globalVariables, Long dataConnectorId, String sqlExpression, Map<String, String> arguments) {
        DssQuery query = this.getPluginQuery(plugin);
        return this.executeSql(query, globalVariables, dataConnectorId, sqlExpression, arguments);
    }

    public String getURIHash(String path) {
        return FileAccessUtils.getHashFromPath(path);
    }

    private DssQuery getQuery(String name) {
        return (DssQuery) this.dssQueryRepository.findByNameIgnoreCase(name).orElseThrow(() -> new QueryNotFoundByName(name));
    }

    private DssQuery getPluginQuery(QueryAdapter plugin) {
        Service annotation = (Service) plugin.getClass().getAnnotation(Service.class);
        String queryName = annotation != null && !annotation.value().isEmpty() ? annotation.value() : plugin.getClass().getSimpleName();
        return this.getQuery(queryName);
    }

    private DssQuery getPluginQuery(FileAccessAdapter plugin) {
        Service annotation = (Service) plugin.getClass().getAnnotation(Service.class);
        String queryName = annotation != null && !annotation.value().isEmpty() ? annotation.value() : plugin.getClass().getSimpleName();
        return this.getQuery(queryName);
    }

    private List<DataConnector> getPluginDataConnectors() {
        return this.dataConnectorService.getAllConnectors();
    }

    private DataConnectorBaseInfo dataConnectorToBaseInfoDto(DataConnector dc) {
        DataConnectorBaseInfo baseInfo = new DataConnectorBaseInfo();
        baseInfo.setId(dc.getId());
        baseInfo.setName(dc.getName());
        baseInfo.setDescription(dc.getDescription());
        baseInfo.setArchive(dc.getArchive());
        baseInfo.setLive(dc.getLive());
        baseInfo.setArchived(dc.getArchived());
        if (dc instanceof JdbcDataConnector) {
            baseInfo.setDbName(((JdbcDataConnector) dc).getDbName());
        }

        return baseInfo;
    }

    private List<String> getAllDataConnectorKeys(DssQuery query) {
        List<DataConnector> dataConnectors = this.getPluginDataConnectors();
        DssQueryImplementation implementation = QueryExecutionUtils.pickActiveImplementation(query, dataConnectors);
        if (implementation == null) {
            throw new DssQueryIsNotImplementedException(query.getName());
        } else {
            return implementation.getQueryImplDataConnectors().stream().map(DssQueryImplDataConnector::getKey).collect(Collectors.toList());
        }
    }

    private NamedDataConnectorResponse getNamedDataConnectors(DssQuery query, String key) {
        List<DataConnector> dataConnectors = this.getPluginDataConnectors();
        DssQueryImplementation implementation = QueryExecutionUtils.pickActiveImplementation(query, dataConnectors);
        if (implementation == null) {
            throw new DssQueryIsNotImplementedException(query.getName());
        } else {
            Optional<DssQueryImplDataConnector> namedDc = implementation.getQueryImplDataConnectors().stream().filter((implDc) -> implDc.getKey().equals(key)).findAny();
            return namedDc.map((implDataConnector) -> new NamedDataConnectorResponse(implDataConnector.getKind().toString(),
                    namedDc.get().getDataConnectors().stream().map(this::dataConnectorToBaseInfoDto).collect(Collectors.toList()))).orElse(null);
        }
    }

    private String getDataConnectorType(DssQuery query, String key) {
        List<DataConnector> courtLinkedConnectors = this.getPluginDataConnectors();
        DssQueryImplementation implementation = QueryExecutionUtils.pickActiveImplementation(query, courtLinkedConnectors);
        if (implementation == null) {
            throw new DssQueryIsNotImplementedException(query.getName());
        } else {
            Optional<DssQueryImplDataConnector> namedDc = implementation.getQueryImplDataConnectors().stream().filter((implDc) -> implDc.getKey().equals(key)).findAny();
            return namedDc.map((implDataConnector) -> implDataConnector.getKind().toString()).orElse(null);
        }
    }

    private Object executeSql(DssQuery query, GlobalVariables globalVariables, Long dataConnectorId, String sqlExpression, Map<String, String> arguments) {
        if (!sqlExpression.toLowerCase().startsWith("select")) {
            throw new RuntimeException("Only SELECT SQL execution is allowed");
        } else {
            DataConnector dataConnector = this.dataConnectorService.getDataConnector(dataConnectorId);
            String expression = QueryExecutionUtils.resolveSqlArguments(query, sqlExpression, arguments, Collections.emptySet());
            return this.dataConnectorService.executeQuery(dataConnector, new ExecuteQueryRequest(expression)).toString();
        }
    }
}
