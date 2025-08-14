package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.court.DataStore;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.utils.DataStoreUtils;
import com.radiant.dto.NameId;
import com.radiant.log.audit.domain.AuditableEntity;
import com.radiant.query.domain.DssQueryImplDataConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryImplConnectorDto {
   private String key;
   private DataConnectorKind kind;
   private List<Long> dataConnectors = new ArrayList();
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private List<NameId> courts = new ArrayList();

   public QueryImplConnectorDto(DssQueryImplDataConnector implDataConnector) {
      this.key = implDataConnector.getKey();
      this.kind = implDataConnector.getKind();
      this.dataConnectors = (List)implDataConnector.getDataConnectors().stream().map(DataConnector::getId).collect(Collectors.toList());
      DataStore dataStoreKind = DataStoreUtils.pickByTypeAndKind(implDataConnector.getQueryImplementation().getQuery().getCaseType(), implDataConnector.getKind());
      Set<NameId> courtSet = (Set)implDataConnector.getDataConnectors().stream().flatMap((dc) -> DataStoreUtils.getAssociatedCourtsByDataStore(dataStoreKind, dc).stream()).map(AuditableEntity::toNamedId).collect(Collectors.toSet());
      this.getCourts().addAll(courtSet);
   }

   public QueryImplConnectorDto(String key, DataConnectorKind kind, List<Long> dataConnectors) {
      this.key = key;
      this.kind = kind;
      this.dataConnectors = dataConnectors;
   }

   public String getKey() {
      return this.key;
   }

   public DataConnectorKind getKind() {
      return this.kind;
   }

   public List<Long> getDataConnectors() {
      return this.dataConnectors;
   }

   public List<NameId> getCourts() {
      return this.courts;
   }

   public void setKey(final String key) {
      this.key = key;
   }

   public void setKind(final DataConnectorKind kind) {
      this.kind = kind;
   }

   public void setDataConnectors(final List<Long> dataConnectors) {
      this.dataConnectors = dataConnectors;
   }

   public QueryImplConnectorDto() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof QueryImplConnectorDto)) {
         return false;
      } else {
         QueryImplConnectorDto other = (QueryImplConnectorDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$key = this.getKey();
            Object other$key = other.getKey();
            if (this$key == null) {
               if (other$key != null) {
                  return false;
               }
            } else if (!this$key.equals(other$key)) {
               return false;
            }

            Object this$kind = this.getKind();
            Object other$kind = other.getKind();
            if (this$kind == null) {
               if (other$kind != null) {
                  return false;
               }
            } else if (!this$kind.equals(other$kind)) {
               return false;
            }

            Object this$dataConnectors = this.getDataConnectors();
            Object other$dataConnectors = other.getDataConnectors();
            if (this$dataConnectors == null) {
               if (other$dataConnectors != null) {
                  return false;
               }
            } else if (!this$dataConnectors.equals(other$dataConnectors)) {
               return false;
            }

            Object this$courts = this.getCourts();
            Object other$courts = other.getCourts();
            if (this$courts == null) {
               if (other$courts != null) {
                  return false;
               }
            } else if (!this$courts.equals(other$courts)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof QueryImplConnectorDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $key = this.getKey();
      result = result * 59 + ($key == null ? 43 : $key.hashCode());
      Object $kind = this.getKind();
      result = result * 59 + ($kind == null ? 43 : $kind.hashCode());
      Object $dataConnectors = this.getDataConnectors();
      result = result * 59 + ($dataConnectors == null ? 43 : $dataConnectors.hashCode());
      Object $courts = this.getCourts();
      result = result * 59 + ($courts == null ? 43 : $courts.hashCode());
      return result;
   }
}
