package com.radiant.integrationFunction.domain.dto;

import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.integrationFunction.domain.IntegrationConnector;
import com.radiant.query.domain.DssQueryImplDataConnector;

public class IntegrationFunctionConnectorDto {
   private String key;
   private DataConnectorKind kind;

   public IntegrationFunctionConnectorDto(IntegrationConnector connector) {
      this.key = connector.getKey();
      this.kind = connector.getKind();
   }

   public IntegrationFunctionConnectorDto(DssQueryImplDataConnector connector) {
      this.key = connector.getKey();
      this.kind = connector.getKind();
   }

   public String getKey() {
      return this.key;
   }

   public DataConnectorKind getKind() {
      return this.kind;
   }

   public void setKey(final String key) {
      this.key = key;
   }

   public void setKind(final DataConnectorKind kind) {
      this.kind = kind;
   }

   public IntegrationFunctionConnectorDto() {
   }
}
