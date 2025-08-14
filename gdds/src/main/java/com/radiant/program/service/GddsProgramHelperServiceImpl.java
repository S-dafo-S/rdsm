package com.radiant.program.service;

import com.radiant.plugin.dto.DataConnectorInfo;
import com.radiant.program.DnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GddsProgramHelperServiceImpl implements ProgramHelperService {
   private static final String ILLEGAL_STATE_MESSAGE = "Program integrations are not available for GDDS";

   public String getIntegrationType(DnodeProgramAdapter program, ProgramRequest request) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public String getIntegrationType(DnodeProgramAdapter program, ProgramRequest request, Long dataConnectorId) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public void executeIntegration(DnodeProgramAdapter program, ProgramRequest request, ProgramResponse response) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public void executeIntegration(DnodeProgramAdapter program, ProgramRequest request, ProgramResponse response, Long dataConnectorId) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public Set<String> getIntegrationParameters(DnodeProgramAdapter program, ProgramRequest request) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public Set<String> getIntegrationParameters(DnodeProgramAdapter program, ProgramRequest request, Long dataConnectorId) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public String getIntegrationParameter(DnodeProgramAdapter program, ProgramRequest request, String key) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public String getIntegrationParameter(DnodeProgramAdapter program, ProgramRequest request, String key, Long dataConnectorId) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public boolean testConnector(Long connectorId) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public List<DataConnectorInfo> getAllConnectors() {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }

   public String getURIHash(String path) {
      throw new IllegalStateException("Program integrations are not available for GDDS");
   }
}
