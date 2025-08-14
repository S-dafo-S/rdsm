package com.radiant.fileAccess.path.dto;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class FileAccessPathsBatchCreateRequest {
   private @NotNull Long connectorId;
   @Nullable
   private String userId;
   @Nullable
   private String userPassword;
   @Nullable
   private String description;
   private @NotNull List<FileAccessPathMapping> paths;

   public Long getConnectorId() {
      return this.connectorId;
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
   public String getDescription() {
      return this.description;
   }

   public List<FileAccessPathMapping> getPaths() {
      return this.paths;
   }

   public void setConnectorId(final Long connectorId) {
      this.connectorId = connectorId;
   }

   public void setUserId(@Nullable final String userId) {
      this.userId = userId;
   }

   public void setUserPassword(@Nullable final String userPassword) {
      this.userPassword = userPassword;
   }

   public void setDescription(@Nullable final String description) {
      this.description = description;
   }

   public void setPaths(final List<FileAccessPathMapping> paths) {
      this.paths = paths;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessPathsBatchCreateRequest)) {
         return false;
      } else {
         FileAccessPathsBatchCreateRequest other = (FileAccessPathsBatchCreateRequest)o;
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

            Object this$description = this.getDescription();
            Object other$description = other.getDescription();
            if (this$description == null) {
               if (other$description != null) {
                  return false;
               }
            } else if (!this$description.equals(other$description)) {
               return false;
            }

            Object this$paths = this.getPaths();
            Object other$paths = other.getPaths();
            if (this$paths == null) {
               if (other$paths != null) {
                  return false;
               }
            } else if (!this$paths.equals(other$paths)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessPathsBatchCreateRequest;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $connectorId = this.getConnectorId();
      result = result * 59 + ($connectorId == null ? 43 : $connectorId.hashCode());
      Object $userId = this.getUserId();
      result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
      Object $userPassword = this.getUserPassword();
      result = result * 59 + ($userPassword == null ? 43 : $userPassword.hashCode());
      Object $description = this.getDescription();
      result = result * 59 + ($description == null ? 43 : $description.hashCode());
      Object $paths = this.getPaths();
      result = result * 59 + ($paths == null ? 43 : $paths.hashCode());
      return result;
   }

   public String toString() {
      return "FileAccessPathsBatchCreateRequest(connectorId=" + this.getConnectorId() + ", userId=" + this.getUserId() + ", userPassword=" + this.getUserPassword() + ", description=" + this.getDescription() + ", paths=" + this.getPaths() + ")";
   }
}
