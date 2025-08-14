package com.radiant.fileAccess.path.domain;

import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.ecrypt.EncryptorConverter;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import com.radiant.util.DateUtils;
import java.util.Date;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
   name = "file_access_path",
   uniqueConstraints = {@UniqueConstraint(
   name = "logical_path_uniq",
   columnNames = {"logical_path"}
)}
)
public class FileAccessPath implements AuditableEntity {
   public static final String LOGICAL_PATH_UNIQ_CONSTRAINT = "logical_path_uniq";
   @Id
   @Column(
      name = "id",
      nullable = false,
      updatable = false
   )
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   private Long id;
   @ManyToOne
   @JoinColumn(
      name = "data_connector",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "file_access_path_data_connector_fk"
)
   )
   private @NotNull DataConnector connector;
   @Column(
      name = "logical_path",
      nullable = false,
      length = 1024
   )
   private @NotNull String logicalPath;
   @Column(
      name = "physical_path",
      nullable = false,
      length = 1024
   )
   private @NotNull String physicalPath;
   @Enumerated(EnumType.STRING)
   @Column(
      name = "status",
      nullable = false
   )
   private @NotNull FileAccessPathStatus status;
   @Column(
      name = "user_id"
   )
   @Nullable
   private String userId;
   @Convert(
      converter = EncryptorConverter.class
   )
   @Column(
      name = "user_password"
   )
   @Nullable
   private String userPassword;
   @Nullable
   @Column(
      name = "description"
   )
   private String description;
   @Column(
      name = "creation_date"
   )
   @Temporal(TemporalType.TIMESTAMP)
   @CreationTimestamp
   private Date creationDate;
   @UpdateTimestamp
   @Temporal(TemporalType.TIMESTAMP)
   @Column(
      name = "update_date"
   )
   private Date updateDate;

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

   public AuditObject toAuditObject() {
      return new AuditObject(AuditObjectType.FILE_ACCESS_PATH, this.id, this.logicalPath);
   }

   public Long getId() {
      return this.id;
   }

   public DataConnector getConnector() {
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

   public void setId(final Long id) {
      this.id = id;
   }

   public void setConnector(final DataConnector connector) {
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

   public void setUserId(@Nullable final String userId) {
      this.userId = userId;
   }

   public void setUserPassword(@Nullable final String userPassword) {
      this.userPassword = userPassword;
   }

   public void setDescription(@Nullable final String description) {
      this.description = description;
   }

   public String toString() {
      return "FileAccessPath(id=" + this.getId() + ", logicalPath=" + this.getLogicalPath() + ", physicalPath=" + this.getPhysicalPath() + ", description=" + this.getDescription() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessPath)) {
         return false;
      } else {
         FileAccessPath other = (FileAccessPath)o;
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

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessPath;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      return result;
   }
}
