package com.radiant.dataConnector.service;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.dataConnector.domain.dto.ExecuteQueryRequest;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface DataConnectorService {
   DataConnectorDto get(Long id);

   List<DataConnectorDto> getAll();

   DataConnectorDto create(DataConnectorDto request, MultipartFile jarFile);

   DataConnectorDto patch(Long id, DataConnectorDto request, MultipartFile jarFile);

   DataConnectorDto update(Long id, DataConnectorDto request, MultipartFile jarFile);

   String executeQueryForSingleString(Long id, ExecuteQueryRequest request);

   JSONArray executeQuery(DataConnector dataConnector, ExecuteQueryRequest request);

   JSONObject executeQuery(DataConnector dataConnector, ExecuteQueryRequest request, @NotNull Integer pageSize, @NotNull Integer pageNum);

   DataConnector getDataConnector(Long id);

   List<DataConnector> getDataConnectors(Collection<Long> ids);

   boolean testConnect(DataConnectorDto request) throws Exception;

   boolean testConnect(Long connectorId) throws Exception;

   String executeCliQuery(CliRequest request);

   List<DataConnector> getAllConnectors();
}
