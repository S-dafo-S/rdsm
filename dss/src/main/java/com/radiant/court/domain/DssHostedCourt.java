package com.radiant.court.domain;

import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.domain.AuditObjectType;
import com.radiant.log.audit.domain.AuditableEntity;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "court_host",
   uniqueConstraints = {@UniqueConstraint(
   name = "court_host_court_uniq",
   columnNames = {"court"}
), @UniqueConstraint(
   name = "court_host_local_id_uniq",
   columnNames = {"local_id"}
)}
)
public class DssHostedCourt extends HostedCourt implements AuditableEntity {
   public static final String COURT_UNIQ_CONSTRAINT = "court_host_court_uniq";
   public static final String LOCAL_ID_UNIQ_CONSTRAINT = "court_host_local_id_uniq";
   @OneToOne
   @JoinColumn(
      name = "court",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "court_host_court_fk"
)
   )
   private DssCourt court;
   @OneToMany(
      cascade = {CascadeType.ALL},
      mappedBy = "hostedCourt",
      orphanRemoval = true
   )
   private List<CourtDataStore> dataStores = new ArrayList();

   public DssHostedCourt(Long localId, String localName, DssCourt court) {
      super(localId, localName);
      this.court = court;
   }

   public AuditObject toAuditObject() {
      return new AuditObject(AuditObjectType.HOSTED_COURT, this.getId(), this.court.getName());
   }

   public DssCourt getCourt() {
      return this.court;
   }

   public List<CourtDataStore> getDataStores() {
      return this.dataStores;
   }

   public void setCourt(final DssCourt court) {
      this.court = court;
   }

   public void setDataStores(final List<CourtDataStore> dataStores) {
      this.dataStores = dataStores;
   }

   public DssHostedCourt() {
   }
}
