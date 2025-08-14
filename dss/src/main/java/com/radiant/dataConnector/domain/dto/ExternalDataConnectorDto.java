package com.radiant.dataConnector.domain.dto;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import javax.annotation.Nullable;

public class ExternalDataConnectorDto extends DataConnectorBaseDto {
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

   public ExternalDataConnectorDto(DataConnector connector) {
      super(connector);
      if (connector instanceof JdbcDataConnector) {
         JdbcDataConnector jdbcConnector = (JdbcDataConnector)connector;
         this.hostname = jdbcConnector.getHostname();
         this.dbName = jdbcConnector.getDbName();
         this.userId = jdbcConnector.getUserId();
         this.userPassword = jdbcConnector.getUserPassword();
         this.port = jdbcConnector.getPort();
      }

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

   public ExternalDataConnectorDto() {
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ExternalDataConnectorDto)) {
         return false;
      } else {
         ExternalDataConnectorDto other = (ExternalDataConnectorDto)o;
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

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof ExternalDataConnectorDto;
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
      return result;
   }
}
