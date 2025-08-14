package com.radiant.region.domain;

import com.radiant.court.domain.DssCourt;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "region",
   uniqueConstraints = {@UniqueConstraint(
   name = "region_name_uniq",
   columnNames = {"name"}
)}
)
public class DssRegion extends RegionBase {
   @ManyToOne
   @JoinColumn(
      name = "parent",
      foreignKey = @ForeignKey(
   name = "region_parent_fk"
)
   )
   private DssRegion parent;
   @OneToMany(
      mappedBy = "region"
   )
   private List<DssCourt> courts = new ArrayList();

   public DssRegion(Long id, String name, String shortName, Long level) {
      super(id, name, shortName, level);
   }

   public DssRegion getParent() {
      return this.parent;
   }

   public List<DssCourt> getCourts() {
      return this.courts;
   }

   public void setParent(final DssRegion parent) {
      this.parent = parent;
   }

   public void setCourts(final List<DssCourt> courts) {
      this.courts = courts;
   }

   public DssRegion() {
   }
}
