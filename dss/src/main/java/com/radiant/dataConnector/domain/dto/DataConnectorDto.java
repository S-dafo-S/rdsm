package com.radiant.dataConnector.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.court.domain.dto.CourtDataStoreDto;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.dataConnector.service.DataConnectorVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@JsonInclude(Include.NON_NULL)
public class DataConnectorDto extends DataConnectorBaseDto {
   @Nullable
   private String hostname;
   @Nullable
   private Integer port;
   @Nullable
   private String userId;
   @Nullable
   private String userPassword;
   @Nullable
   private String dbName;
   @Nullable
   private String customJdbcUrl;
   @Nullable
   private String dbVersion;
   @Nullable
   private String jarFilename;
   @Nullable
   private String urlPrefix;
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private List<CourtDataStoreDto> courtDataStores = new ArrayList();

   public DataConnectorDto(DataConnector connector) {
      super(connector);
      this.courtDataStores = (List)connector.getCourtDataStores().stream().map(CourtDataStoreDto::new).collect(Collectors.toList());
      connector.accept(new FillDtoVisitor());
   }

   @Nullable
   public String getHostname() {
      return this.hostname;
   }

   @Nullable
   public Integer getPort() {
      return this.port;
   }

   @Nullable
   public String getUserId() {
      return this.userId;
   }

   @Nullable
   public String getUserPassword() {
      return this.userPassword;
   }

   @Nullable
   public String getDbName() {
      return this.dbName;
   }

   @Nullable
   public String getCustomJdbcUrl() {
      return this.customJdbcUrl;
   }

   @Nullable
   public String getDbVersion() {
      return this.dbVersion;
   }

   @Nullable
   public String getJarFilename() {
      return this.jarFilename;
   }

   @Nullable
   public String getUrlPrefix() {
      return this.urlPrefix;
   }

   public List<CourtDataStoreDto> getCourtDataStores() {
      return this.courtDataStores;
   }

   public void setHostname(@Nullable final String hostname) {
      this.hostname = hostname;
   }

   public void setPort(@Nullable final Integer port) {
      this.port = port;
   }

   public void setUserId(@Nullable final String userId) {
      this.userId = userId;
   }

   public void setUserPassword(@Nullable final String userPassword) {
      this.userPassword = userPassword;
   }

   public void setDbName(@Nullable final String dbName) {
      this.dbName = dbName;
   }

   public void setCustomJdbcUrl(@Nullable final String customJdbcUrl) {
      this.customJdbcUrl = customJdbcUrl;
   }

   public void setDbVersion(@Nullable final String dbVersion) {
      this.dbVersion = dbVersion;
   }

   public void setJarFilename(@Nullable final String jarFilename) {
      this.jarFilename = jarFilename;
   }

   public void setUrlPrefix(@Nullable final String urlPrefix) {
      this.urlPrefix = urlPrefix;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DataConnectorDto)) {
         return false;
      } else {
         DataConnectorDto other = (DataConnectorDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$port = this.getPort();
            Object other$port = other.getPort();
            if (this$port == null) {
               if (other$port != null) {
                  return false;
               }
            } else if (!this$port.equals(other$port)) {
               return false;
            }

            Object this$hostname = this.getHostname();
            Object other$hostname = other.getHostname();
            if (this$hostname == null) {
               if (other$hostname != null) {
                  return false;
               }
            } else if (!this$hostname.equals(other$hostname)) {
               return false;
            }

            Object this$userId = this.getUserId();
            Object other$userId = other.getUserId();
            if (this$userId == null) {
               if (other$userId != null) {
                  return false;
               }
            } else if (!this$userId.equals(other$userId)) {
               return false;
            }

            Object this$userPassword = this.getUserPassword();
            Object other$userPassword = other.getUserPassword();
            if (this$userPassword == null) {
               if (other$userPassword != null) {
                  return false;
               }
            } else if (!this$userPassword.equals(other$userPassword)) {
               return false;
            }

            Object this$dbName = this.getDbName();
            Object other$dbName = other.getDbName();
            if (this$dbName == null) {
               if (other$dbName != null) {
                  return false;
               }
            } else if (!this$dbName.equals(other$dbName)) {
               return false;
            }

            Object this$customJdbcUrl = this.getCustomJdbcUrl();
            Object other$customJdbcUrl = other.getCustomJdbcUrl();
            if (this$customJdbcUrl == null) {
               if (other$customJdbcUrl != null) {
                  return false;
               }
            } else if (!this$customJdbcUrl.equals(other$customJdbcUrl)) {
               return false;
            }

            Object this$dbVersion = this.getDbVersion();
            Object other$dbVersion = other.getDbVersion();
            if (this$dbVersion == null) {
               if (other$dbVersion != null) {
                  return false;
               }
            } else if (!this$dbVersion.equals(other$dbVersion)) {
               return false;
            }

            Object this$jarFilename = this.getJarFilename();
            Object other$jarFilename = other.getJarFilename();
            if (this$jarFilename == null) {
               if (other$jarFilename != null) {
                  return false;
               }
            } else if (!this$jarFilename.equals(other$jarFilename)) {
               return false;
            }

            Object this$urlPrefix = this.getUrlPrefix();
            Object other$urlPrefix = other.getUrlPrefix();
            if (this$urlPrefix == null) {
               if (other$urlPrefix != null) {
                  return false;
               }
            } else if (!this$urlPrefix.equals(other$urlPrefix)) {
               return false;
            }

            Object this$courtDataStores = this.getCourtDataStores();
            Object other$courtDataStores = other.getCourtDataStores();
            if (this$courtDataStores == null) {
               if (other$courtDataStores != null) {
                  return false;
               }
            } else if (!this$courtDataStores.equals(other$courtDataStores)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DataConnectorDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $port = this.getPort();
      result = result * 59 + ($port == null ? 43 : $port.hashCode());
      Object $hostname = this.getHostname();
      result = result * 59 + ($hostname == null ? 43 : $hostname.hashCode());
      Object $userId = this.getUserId();
      result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
      Object $userPassword = this.getUserPassword();
      result = result * 59 + ($userPassword == null ? 43 : $userPassword.hashCode());
      Object $dbName = this.getDbName();
      result = result * 59 + ($dbName == null ? 43 : $dbName.hashCode());
      Object $customJdbcUrl = this.getCustomJdbcUrl();
      result = result * 59 + ($customJdbcUrl == null ? 43 : $customJdbcUrl.hashCode());
      Object $dbVersion = this.getDbVersion();
      result = result * 59 + ($dbVersion == null ? 43 : $dbVersion.hashCode());
      Object $jarFilename = this.getJarFilename();
      result = result * 59 + ($jarFilename == null ? 43 : $jarFilename.hashCode());
      Object $urlPrefix = this.getUrlPrefix();
      result = result * 59 + ($urlPrefix == null ? 43 : $urlPrefix.hashCode());
      Object $courtDataStores = this.getCourtDataStores();
      result = result * 59 + ($courtDataStores == null ? 43 : $courtDataStores.hashCode());
      return result;
   }

   public DataConnectorDto() {
   }

   public String toString() {
      return "DataConnectorDto(super=" + super.toString() + ", hostname=" + this.getHostname() + ", port=" + this.getPort() + ", dbName=" + this.getDbName() + ", customJdbcUrl=" + this.getCustomJdbcUrl() + ", dbVersion=" + this.getDbVersion() + ")";
   }

   private class FillDtoVisitor implements DataConnectorVisitor<Void> {
      private FillDtoVisitor() {
      }

      public Void visit(JdbcDataConnector jdbcDataConnector) {
         DataConnectorDto.this.hostname = jdbcDataConnector.getHostname();
         DataConnectorDto.this.dbName = jdbcDataConnector.getDbName();
         DataConnectorDto.this.userId = jdbcDataConnector.getUserId();
         DataConnectorDto.this.port = jdbcDataConnector.getPort();
         DataConnectorDto.this.dbVersion = jdbcDataConnector.getDbVersion();
         DataConnectorDto.this.customJdbcUrl = jdbcDataConnector.getCustomJdbcUrl();
         return null;
      }

      public Void visit(LocalFileSystemDataConnector localFSDataConnector) {
         return null;
      }

      public Void visit(MinioDataConnector minioDataConnector) {
         return null;
      }
   }
}
