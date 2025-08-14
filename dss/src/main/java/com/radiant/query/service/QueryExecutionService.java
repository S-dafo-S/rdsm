package com.radiant.query.service;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.dto.QueryTestExecutionDto;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;

public interface QueryExecutionService {
   Object execute(String queryName, Map<String, String> arguments, @Nullable HttpServletResponse servletResponse);

   String testExecution(Long queryId, QueryTestExecutionDto testExecutionDto);

   DssQueryImplementation getQueryImplementation(String queryName);

   DssQueryImplementation getQueryImplementation(String queryName, DataConnector dataConnector);

   Object executeImpl(DssQueryImplementation implementation, List<DataConnector> dataConnectors, Map<String, String> arguments, @Nullable HttpServletResponse servletResponse);
}
