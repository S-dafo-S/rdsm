package com.radiant.fileAccess.path.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.domain.FileAccessPathStatus;
import com.radiant.util.DateUtils;
import java.util.Date;
import javax.annotation.Nullable;

@JsonInclude(Include.NON_NULL)
public class FileAccessPathDto {
   private Long id;
   private DataConnectorDto connector;
   private String logicalPath;
   private String physicalPath;
   private FileAccessPathStatus status;
   @Nullable
   private String description;
   @Nullable
   private String userId;
   @Nullable
   private String userPassword;
   @Nullable
   private Date creationDate;
   @Nullable
   private Date updateDate;

   public FileAccessPathDto(FileAccessPath path) {
      this.id = path.getId();
      this.connector = new DataConnectorDto(path.getConnector());
      this.logicalPath = path.getLogicalPath();
      this.physicalPath = path.getPhysicalPath();
      this.status = path.getStatus();
      this.description = path.getDescription();
      this.userId = path.getUserId();
      this.setCreationDate(path.getCreationDate());
      this.setUpdateDate(path.getUpdateDate());
   }

   public Date getCreationDate() {
      return DateUtils.cloneDate(this.creationDate);
   }

   public void setCreationDate(Date creationDate) {
      this.creationDate = DateUtils.cloneDate(creationDate);
   }

   public Date getUpdateDate() {
      return DateUtils.cloneDate(this.updateDate);
   }

   public void setUpdateDate(Date updateDate) {
      this.updateDate = DateUtils.cloneDate(updateDate);
   }

   public Long getId() {
      return this.id;
   }

   public DataConnectorDto getConnector() {
      return this.connector;
   }

   public String getLogicalPath() {
      return this.logicalPath;
   }

   public String getPhysicalPath() {
      return this.physicalPath;
   }

   public FileAccessPathStatus getStatus() {
      return this.status;
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

   public void setId(final Long id) {
      this.id = id;
   }

   public void setConnector(final DataConnectorDto connector) {
      this.connector = connector;
   }

   public void setLogicalPath(final String logicalPath) {
      this.logicalPath = logicalPath;
   }

   public void setPhysicalPath(final String physicalPath) {
      this.physicalPath = physicalPath;
   }

   public void setStatus(final FileAccessPathStatus status) {
      this.status = status;
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

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessPathDto)) {
         return false;
      } else {
         FileAccessPathDto other = (FileAccessPathDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$id = this.getId();
            Object other$id = other.getId();
            if (this$id == null) {
               if (other$id != null) {
                  return false;
               }
            } else if (!this$id.equals(other$id)) {
               return false;
            }

            Object this$connector = this.getConnector();
            Object other$connector = other.getConnector();
            if (this$connector == null) {
               if (other$connector != null) {
                  return false;
               }
            } else if (!this$connector.equals(other$connector)) {
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

            Object this$status = this.getStatus();
            Object other$status = other.getStatus();
            if (this$status == null) {
               if (other$status != null) {
                  return false;
               }
            } else if (!this$status.equals(other$status)) {
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

            Object this$creationDate = this.getCreationDate();
            Object other$creationDate = other.getCreationDate();
            if (this$creationDate == null) {
               if (other$creationDate != null) {
                  return false;
               }
            } else if (!this$creationDate.equals(other$creationDate)) {
               return false;
            }

            Object this$updateDate = this.getUpdateDate();
            Object other$updateDate = other.getUpdateDate();
            if (this$updateDate == null) {
               if (other$updateDate != null) {
                  return false;
               }
            } else if (!this$updateDate.equals(other$updateDate)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessPathDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $connector = this.getConnector();
      result = result * 59 + ($connector == null ? 43 : $connector.hashCode());
      Object $logicalPath = this.getLogicalPath();
      result = result * 59 + ($logicalPath == null ? 43 : $logicalPath.hashCode());
      Object $physicalPath = this.getPhysicalPath();
      result = result * 59 + ($physicalPath == null ? 43 : $physicalPath.hashCode());
      Object $status = this.getStatus();
      result = result * 59 + ($status == null ? 43 : $status.hashCode());
      Object $description = this.getDescription();
      result = result * 59 + ($description == null ? 43 : $description.hashCode());
      Object $userId = this.getUserId();
      result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
      Object $userPassword = this.getUserPassword();
      result = result * 59 + ($userPassword == null ? 43 : $userPassword.hashCode());
      Object $creationDate = this.getCreationDate();
      result = result * 59 + ($creationDate == null ? 43 : $creationDate.hashCode());
      Object $updateDate = this.getUpdateDate();
      result = result * 59 + ($updateDate == null ? 43 : $updateDate.hashCode());
      return result;
   }

   public FileAccessPathDto() {
   }

   public String toString() {
      return "FileAccessPathDto(id=" + this.getId() + ")";
   }
}
