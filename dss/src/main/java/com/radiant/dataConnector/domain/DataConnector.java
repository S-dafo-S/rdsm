package com.radiant.dataConnector.domain;

import com.radiant.court.domain.CourtDataStore;
import com.radiant.dataConnector.service.DataConnectorVisitor;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import com.radiant.util.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
   name = "data_connector"
)
@Inheritance(
   strategy = InheritanceType.JOINED
)
public abstract class DataConnector implements AuditableEntity {
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
   @Column(
      name = "name",
      nullable = false,
      length = 128
   )
   private @NotNull String name;
   @Column(
      name = "live",
      nullable = false
   )
   private @NotNull Boolean live;
   @Column(
      name = "archive",
      nullable = false
   )
   private @NotNull Boolean archive;
   @Column(
      name = "archived",
      nullable = false
   )
   @ColumnDefault("false")
   private @NotNull Boolean archived;
   @Nullable
   @Column(
      name = "description"
   )
   private String description;
   @OneToMany(
      mappedBy = "connector"
   )
   private List<FileAccessPath> fileAccessPaths = new ArrayList();
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
   @OneToMany(
      mappedBy = "dataConnector"
   )
   private List<CourtDataStore> courtDataStores = new ArrayList();

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

   public abstract @NotNull DataConnectorType getType();

   public AuditObject toAuditObject() {
      return new AuditObject(AuditObjectType.DATA_CONNECTOR, this.id, this.name);
   }

   public abstract <T> T accept(DataConnectorVisitor<T> visitor);

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
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
   public String getDescription() {
      return this.description;
   }

   public List<FileAccessPath> getFileAccessPaths() {
      return this.fileAccessPaths;
   }

   public List<CourtDataStore> getCourtDataStores() {
      return this.courtDataStores;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setName(final String name) {
      this.name = name;
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

   public void setDescription(@Nullable final String description) {
      this.description = description;
   }

   public void setFileAccessPaths(final List<FileAccessPath> fileAccessPaths) {
      this.fileAccessPaths = fileAccessPaths;
   }

   public void setCourtDataStores(final List<CourtDataStore> courtDataStores) {
      this.courtDataStores = courtDataStores;
   }

   public String toString() {
      return "DataConnector(id=" + this.getId() + ", name=" + this.getName() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DataConnector)) {
         return false;
      } else {
         DataConnector other = (DataConnector)o;
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

            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
               if (other$name != null) {
                  return false;
               }
            } else if (!this$name.equals(other$name)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DataConnector;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      return result;
   }

   protected DataConnector() {
   }
}
