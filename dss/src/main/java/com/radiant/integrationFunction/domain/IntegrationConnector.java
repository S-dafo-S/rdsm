package com.radiant.integrationFunction.domain;

import com.radiant.dataConnector.domain.DataConnectorKind;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class IntegrationConnector {
   @Column(
      name = "key",
      nullable = false
   )
   private String key;
   @Enumerated(EnumType.STRING)
   @Column(
      name = "kind",
      nullable = false
   )
   private DataConnectorKind kind;

   public IntegrationConnector(String key, DataConnectorKind kind) {
      this.key = key;
      this.kind = kind;
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

   public IntegrationConnector() {
   }
}
