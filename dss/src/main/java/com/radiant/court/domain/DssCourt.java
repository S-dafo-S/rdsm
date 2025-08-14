package com.radiant.court.domain;

import com.radiant.region.domain.DssRegion;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "court",
   uniqueConstraints = {@UniqueConstraint(
   name = "court_name_uniq",
   columnNames = {"name"}
)}
)
public class DssCourt extends CourtBase {
   public static final String NAME_UNIQ_CONSTRAINT = "court_name_uniq";
   @ManyToOne
   @JoinColumn(
      name = "region",
      foreignKey = @ForeignKey(
   name = "court_region_fk"
)
   )
   private DssRegion region;
   @OneToOne(
      mappedBy = "court"
   )
   private DssHostedCourt hostedCourt;

   public DssCourt(Long id, String name, Long level, DssRegion region) {
      super(id, name, level);
      this.region = region;
   }

   public DssRegion getRegion() {
      return this.region;
   }

   public DssHostedCourt getHostedCourt() {
      return this.hostedCourt;
   }

   public DssCourt() {
   }
}
