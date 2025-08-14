package com.radiant.dataConnector.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.domain.DataConnectorType;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.dataConnector.service.DataConnectorVisitor;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

public class DataConnectorBaseDto {
   private Long id;
   private String name;
   private DataConnectorKind kind;
   private DataConnectorType type;
   private Boolean live;
   private Boolean archive;
   private Boolean archived;
   @Nullable
   private String dbName;
   @Nullable
   private String endpoint;
   @Nullable
   private String accessKeyId;
   @Nullable
   private String accessKeySecret;
   @Nullable
   private String bucketName;
   @Nullable
   private String awsRegion;
   @Nullable
   private String description;
   @Nullable
   private List<Long> hostedCourts = new ArrayList();
   @Nullable
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date creationDate;
   @Nullable
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private Date updateDate;

   public DataConnectorBaseDto(DataConnector connector) {
      this.id = connector.getId();
      this.name = connector.getName();
      this.type = connector.getType();
      this.description = connector.getDescription();
      this.live = connector.getLive();
      this.archive = connector.getArchive();
      this.archived = connector.getArchived();
      this.setCreationDate(connector.getCreationDate());
      this.setUpdateDate(connector.getUpdateDate());
      connector.accept(new FillDtoVisitor(this));
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

   public String getName() {
      return this.name;
   }

   public DataConnectorKind getKind() {
      return this.kind;
   }

   public DataConnectorType getType() {
      return this.type;
   }

   public Boolean getLive() {
      return this.live;
   }

   public Boolean getArchive() {
      return this.archive;
   }

   public Boolean getArchived() {
      return this.archived;
   }

   @Nullable
   public String getDbName() {
      return this.dbName;
   }

   @Nullable
   public String getEndpoint() {
      return this.endpoint;
   }

   @Nullable
   public String getAccessKeyId() {
      return this.accessKeyId;
   }

   @Nullable
   public String getAccessKeySecret() {
      return this.accessKeySecret;
   }

   @Nullable
   public String getBucketName() {
      return this.bucketName;
   }

   @Nullable
   public String getAwsRegion() {
      return this.awsRegion;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nullable
   public List<Long> getHostedCourts() {
      return this.hostedCourts;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setKind(final DataConnectorKind kind) {
      this.kind = kind;
   }

   public void setType(final DataConnectorType type) {
      this.type = type;
   }

   public void setLive(final Boolean live) {
      this.live = live;
   }

   public void setArchive(final Boolean archive) {
      this.archive = archive;
   }

   public void setArchived(final Boolean archived) {
      this.archived = archived;
   }

   public void setDbName(@Nullable final String dbName) {
      this.dbName = dbName;
   }

   public void setEndpoint(@Nullable final String endpoint) {
      this.endpoint = endpoint;
   }

   public void setAccessKeyId(@Nullable final String accessKeyId) {
      this.accessKeyId = accessKeyId;
   }

   public void setAccessKeySecret(@Nullable final String accessKeySecret) {
      this.accessKeySecret = accessKeySecret;
   }

   public void setBucketName(@Nullable final String bucketName) {
      this.bucketName = bucketName;
   }

   public void setAwsRegion(@Nullable final String awsRegion) {
      this.awsRegion = awsRegion;
   }

   public void setDescription(@Nullable final String description) {
      this.description = description;
   }

   public void setHostedCourts(@Nullable final List<Long> hostedCourts) {
      this.hostedCourts = hostedCourts;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DataConnectorBaseDto)) {
         return false;
      } else {
         DataConnectorBaseDto other = (DataConnectorBaseDto)o;
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

            Object this$live = this.getLive();
            Object other$live = other.getLive();
            if (this$live == null) {
               if (other$live != null) {
                  return false;
               }
            } else if (!this$live.equals(other$live)) {
               return false;
            }

            Object this$archive = this.getArchive();
            Object other$archive = other.getArchive();
            if (this$archive == null) {
               if (other$archive != null) {
                  return false;
               }
            } else if (!this$archive.equals(other$archive)) {
               return false;
            }

            Object this$archived = this.getArchived();
            Object other$archived = other.getArchived();
            if (this$archived == null) {
               if (other$archived != null) {
                  return false;
               }
            } else if (!this$archived.equals(other$archived)) {
               return false;
            }

            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
               if (other$name != null) {
                  return false;
               }
            } else if (!this$name.equals(other$name)) {
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

            Object this$type = this.getType();
            Object other$type = other.getType();
            if (this$type == null) {
               if (other$type != null) {
                  return false;
               }
            } else if (!this$type.equals(other$type)) {
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

            Object this$endpoint = this.getEndpoint();
            Object other$endpoint = other.getEndpoint();
            if (this$endpoint == null) {
               if (other$endpoint != null) {
                  return false;
               }
            } else if (!this$endpoint.equals(other$endpoint)) {
               return false;
            }

            Object this$accessKeyId = this.getAccessKeyId();
            Object other$accessKeyId = other.getAccessKeyId();
            if (this$accessKeyId == null) {
               if (other$accessKeyId != null) {
                  return false;
               }
            } else if (!this$accessKeyId.equals(other$accessKeyId)) {
               return false;
            }

            Object this$accessKeySecret = this.getAccessKeySecret();
            Object other$accessKeySecret = other.getAccessKeySecret();
            if (this$accessKeySecret == null) {
               if (other$accessKeySecret != null) {
                  return false;
               }
            } else if (!this$accessKeySecret.equals(other$accessKeySecret)) {
               return false;
            }

            Object this$bucketName = this.getBucketName();
            Object other$bucketName = other.getBucketName();
            if (this$bucketName == null) {
               if (other$bucketName != null) {
                  return false;
               }
            } else if (!this$bucketName.equals(other$bucketName)) {
               return false;
            }

            Object this$awsRegion = this.getAwsRegion();
            Object other$awsRegion = other.getAwsRegion();
            if (this$awsRegion == null) {
               if (other$awsRegion != null) {
                  return false;
               }
            } else if (!this$awsRegion.equals(other$awsRegion)) {
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

            Object this$hostedCourts = this.getHostedCourts();
            Object other$hostedCourts = other.getHostedCourts();
            if (this$hostedCourts == null) {
               if (other$hostedCourts != null) {
                  return false;
               }
            } else if (!this$hostedCourts.equals(other$hostedCourts)) {
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
      return other instanceof DataConnectorBaseDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $live = this.getLive();
      result = result * 59 + ($live == null ? 43 : $live.hashCode());
      Object $archive = this.getArchive();
      result = result * 59 + ($archive == null ? 43 : $archive.hashCode());
      Object $archived = this.getArchived();
      result = result * 59 + ($archived == null ? 43 : $archived.hashCode());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $kind = this.getKind();
      result = result * 59 + ($kind == null ? 43 : $kind.hashCode());
      Object $type = this.getType();
      result = result * 59 + ($type == null ? 43 : $type.hashCode());
      Object $dbName = this.getDbName();
      result = result * 59 + ($dbName == null ? 43 : $dbName.hashCode());
      Object $endpoint = this.getEndpoint();
      result = result * 59 + ($endpoint == null ? 43 : $endpoint.hashCode());
      Object $accessKeyId = this.getAccessKeyId();
      result = result * 59 + ($accessKeyId == null ? 43 : $accessKeyId.hashCode());
      Object $accessKeySecret = this.getAccessKeySecret();
      result = result * 59 + ($accessKeySecret == null ? 43 : $accessKeySecret.hashCode());
      Object $bucketName = this.getBucketName();
      result = result * 59 + ($bucketName == null ? 43 : $bucketName.hashCode());
      Object $awsRegion = this.getAwsRegion();
      result = result * 59 + ($awsRegion == null ? 43 : $awsRegion.hashCode());
      Object $description = this.getDescription();
      result = result * 59 + ($description == null ? 43 : $description.hashCode());
      Object $hostedCourts = this.getHostedCourts();
      result = result * 59 + ($hostedCourts == null ? 43 : $hostedCourts.hashCode());
      Object $creationDate = this.getCreationDate();
      result = result * 59 + ($creationDate == null ? 43 : $creationDate.hashCode());
      Object $updateDate = this.getUpdateDate();
      result = result * 59 + ($updateDate == null ? 43 : $updateDate.hashCode());
      return result;
   }

   public DataConnectorBaseDto() {
   }

   public String toString() {
      return "DataConnectorBaseDto(id=" + this.getId() + ", name=" + this.getName() + ", dbName=" + this.getDbName() + ")";
   }

   private static class FillDtoVisitor implements DataConnectorVisitor<DataConnectorBaseDto> {
      private DataConnectorBaseDto dto;

      FillDtoVisitor(DataConnectorBaseDto dto) {
         this.dto = dto;
      }

      public DataConnectorBaseDto visit(JdbcDataConnector jdbcDataConnector) {
         this.dto.dbName = jdbcDataConnector.getDbName();
         this.dto.kind = DataConnectorKind.DB;
         return this.dto;
      }

      public DataConnectorBaseDto visit(LocalFileSystemDataConnector localFSDataConnector) {
         this.dto.kind = DataConnectorKind.DOCUMENT;
         return this.dto;
      }

      public DataConnectorBaseDto visit(MinioDataConnector minioDataConnector) {
         this.dto.kind = DataConnectorKind.DOCUMENT;
         this.dto.endpoint = minioDataConnector.getEndpoint();
         this.dto.accessKeyId = minioDataConnector.getAccessKeyId();
         this.dto.bucketName = minioDataConnector.getBucketName();
         return this.dto;
      }
   }
}
