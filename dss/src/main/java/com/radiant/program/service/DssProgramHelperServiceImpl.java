package com.radiant.program.service;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.fileAccess.FileAccessUtils;
import com.radiant.plugin.dto.DataConnectorInfo;
import com.radiant.program.DnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import com.radiant.program.dto.ProgramType;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.service.QueryExecutionService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DssProgramHelperServiceImpl implements ProgramHelperService {
   private static final Logger LOG = LoggerFactory.getLogger(DssProgramHelperServiceImpl.class);
   @Autowired
   private QueryExecutionService queryExecutionService;
   @Autowired
   private DataConnectorService dataConnectorService;

   public String getIntegrationType(DnodeProgramAdapter program, ProgramRequest request) {
      LOG.trace("Helper method getIntegrationType called for program: {}", program.getClass().getSimpleName());
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program));
      return implementation == null ? null : implementation.getLanguage().name();
   }

   public String getIntegrationType(DnodeProgramAdapter program, ProgramRequest request, Long dataConnectorId) {
      LOG.trace("Helper method getIntegrationType called for program: {}, data connector id: {}", program.getClass().getSimpleName(), dataConnectorId);
      DataConnector dataConnector = this.dataConnectorService.getDataConnector(dataConnectorId);
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program), dataConnector);
      return implementation == null ? null : implementation.getLanguage().name();
   }

   public void executeIntegration(DnodeProgramAdapter program, ProgramRequest request, ProgramResponse response) {
      if (request.getProgramType() == ProgramType.QUERY) {
         Object executionResult = this.queryExecutionService.execute(this.getProgramName(program), request.getUrlParams(), response.getServletResponse());
         if (request.getContentType().equals("application/json")) {
            response.addAllResult(executionResult);
         } else {
            if (!request.getContentType().equals("application/octet-stream")) {
               throw new IllegalStateException("Unknown program content type");
            }

            LOG.info("Executed file read for integration of program {}", request.getProgramName());
         }

      } else {
         throw new IllegalStateException("Unexpected Echo Program type: " + request.getProgramType());
      }
   }

   public void executeIntegration(DnodeProgramAdapter program, ProgramRequest request, ProgramResponse response, Long dataConnectorId) {
      if (request.getProgramType() == ProgramType.QUERY) {
         DataConnector dataConnector = this.dataConnectorService.getDataConnector(dataConnectorId);
         DssQueryImplementation impl = this.queryExecutionService.getQueryImplementation(this.getProgramName(program), dataConnector);
         Object executionResult = this.queryExecutionService.executeImpl(impl, Collections.singletonList(dataConnector), request.getUrlParams(), response.getServletResponse());
         if (request.getContentType().equals("application/json")) {
            response.addAllResult(executionResult);
         } else {
            if (!request.getContentType().equals("application/octet-stream")) {
               throw new IllegalStateException("Unknown program content type");
            }

            LOG.info("Executed file read for integration of program {}", request.getProgramName());
         }

      } else {
         throw new IllegalStateException("Unexpected Echo Program type: " + request.getProgramType());
      }
   }

   public Set<String> getIntegrationParameters(DnodeProgramAdapter program, ProgramRequest request) {
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program));
      return implementation == null ? Collections.emptySet() : implementation.getParameters().keySet();
   }

   public Set<String> getIntegrationParameters(DnodeProgramAdapter program, ProgramRequest request, Long dataConnectorId) {
      DataConnector dataConnector = this.dataConnectorService.getDataConnector(dataConnectorId);
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program), dataConnector);
      return implementation == null ? Collections.emptySet() : implementation.getParameters().keySet();
   }

   public String getIntegrationParameter(DnodeProgramAdapter program, ProgramRequest request, @NotNull String key) {
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program));
      return implementation == null ? null : (String)implementation.getParameters().get(key);
   }

   public String getIntegrationParameter(DnodeProgramAdapter program, ProgramRequest request, String key, Long dataConnectorId) {
      DataConnector dataConnector = this.dataConnectorService.getDataConnector(dataConnectorId);
      DssQueryImplementation implementation = this.queryExecutionService.getQueryImplementation(this.getProgramName(program), dataConnector);
      return implementation == null ? null : (String)implementation.getParameters().get(key);
   }

   public boolean testConnector(Long connectorId) {
      try {
         return this.dataConnectorService.testConnect(connectorId);
      } catch (Exception e) {
         LOG.error("Test connection failed for connector {}", connectorId, e);
         return false;
      }
   }

   public List<DataConnectorInfo> getAllConnectors() {
      return (List)this.dataConnectorService.getAll().stream().map(this::mapToDataConnectorInfo).collect(Collectors.toList());
   }

   public String getURIHash(String path) {
      return FileAccessUtils.getHashFromPath(path);
   }

   private String getProgramName(DnodeProgramAdapter program) {
      return program.getClass().getSimpleName().replaceFirst("^(?i:DNode)", "");
   }

   private DataConnectorInfo mapToDataConnectorInfo(DataConnectorDto dc) {
      DataConnectorInfo result = new DataConnectorInfo();
      result.setId(dc.getId());
      result.setName(dc.getName());
      result.setLive(dc.getLive());
      result.setArchive(dc.getArchive());
      result.setArchived(dc.getArchived());
      result.setDbName(dc.getDbName());
      result.setDescription(dc.getDescription());
      result.setKind(dc.getKind().name());
      result.setHostname(dc.getHostname());
      result.setPort(dc.getPort());
      result.setUserId(dc.getUserId());
      result.setUserPassword(dc.getUserPassword());
      return result;
   }
}
