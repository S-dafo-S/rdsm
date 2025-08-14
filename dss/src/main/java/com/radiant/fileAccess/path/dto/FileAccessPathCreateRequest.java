package com.radiant.fileAccess.path.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class FileAccessPathCreateRequest {
   private @NotNull Long connectorId;
   private @NotEmpty String logicalPath;
   private @NotEmpty String physicalPath;
   @Nullable
   private String description;
   @Nullable
   private String userId;
   @Nullable
   private String userPassword;

   public Long getConnectorId() {
      return this.connectorId;
   }

   public String getLogicalPath() {
      return this.logicalPath;
   }

   public String getPhysicalPath() {
      return this.physicalPath;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nullable
   public String getUserId() {
      return this.userId;
   }

   @Nullable
   public String getUserPassword() {
      return this.userPassword;
   }

   public void setConnectorId(final Long connectorId) {
      this.connectorId = connectorId;
   }

   public void setLogicalPath(final String logicalPath) {
      this.logicalPath = logicalPath;
   }

   public void setPhysicalPath(final String physicalPath) {
      this.physicalPath = physicalPath;
   }

   public void setDescription(@Nullable final String description) {
      this.description = description;
   }

   public void setUserId(@Nullable final String userId) {
      this.userId = userId;
   }

   public void setUserPassword(@Nullable final String userPassword) {
      this.userPassword = userPassword;
   }

   public String toString() {
      return "FileAccessPathCreateRequest(connectorId=" + this.getConnectorId() + ", logicalPath=" + this.getLogicalPath() + ", physicalPath=" + this.getPhysicalPath() + ", description=" + this.getDescription() + ", userId=" + this.getUserId() + ", userPassword=" + this.getUserPassword() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessPathCreateRequest)) {
         return false;
      } else {
         FileAccessPathCreateRequest other = (FileAccessPathCreateRequest)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$connectorId = this.getConnectorId();
            Object other$connectorId = other.getConnectorId();
            if (this$connectorId == null) {
               if (other$connectorId != null) {
                  return false;
               }
            } else if (!this$connectorId.equals(other$connectorId)) {
               return false;
            }

            Object this$logicalPath = this.getLogicalPath();
            Object other$logicalPath = other.getLogicalPath();
            if (this$logicalPath == null) {
               if (other$logicalPath != null) {
                  return false;
               }
            } else if (!this$logicalPath.equals(other$logicalPath)) {
               return false;
            }

            Object this$physicalPath = this.getPhysicalPath();
            Object other$physicalPath = other.getPhysicalPath();
            if (this$physicalPath == null) {
               if (other$physicalPath != null) {
                  return false;
               }
            } else if (!this$physicalPath.equals(other$physicalPath)) {
               return false;
            }

            Object this$description = this.getDescription();
            Object other$description = other.getDescription();
            if (this$description == null) {
               if (other$description != null) {
                  return false;
               }
            } else if (!this$description.equals(other$description)) {
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

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessPathCreateRequest;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $connectorId = this.getConnectorId();
      result = result * 59 + ($connectorId == null ? 43 : $connectorId.hashCode());
      Object $logicalPath = this.getLogicalPath();
      result = result * 59 + ($logicalPath == null ? 43 : $logicalPath.hashCode());
      Object $physicalPath = this.getPhysicalPath();
      result = result * 59 + ($physicalPath == null ? 43 : $physicalPath.hashCode());
      Object $description = this.getDescription();
      result = result * 59 + ($description == null ? 43 : $description.hashCode());
      Object $userId = this.getUserId();
      result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
      Object $userPassword = this.getUserPassword();
      result = result * 59 + ($userPassword == null ? 43 : $userPassword.hashCode());
      return result;
   }
}
