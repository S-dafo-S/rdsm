package com.radiant.region.domain;

import com.radiant.court.domain.GddsCourt;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
   name = "region",
   uniqueConstraints = {@UniqueConstraint(
   name = "region_name_uniq",
   columnNames = {"name", "outdated"}
)}
)
public class GddsRegion extends RegionBase {
   public static final String GDDS_REGION_NAME_UNIQ = "region_name_uniq";
   @ColumnDefault("false")
   @Column(
      name = "outdated",
      nullable = false
   )
   private Boolean outdated = false;
   @ManyToOne
   @JoinColumn(
      name = "parent",
      foreignKey = @ForeignKey(
   name = "region_parent_fk"
)
   )
   private GddsRegion parent;
   @OneToMany(
      cascade = {CascadeType.ALL},
      mappedBy = "region",
      orphanRemoval = true
   )
   @OrderColumn(
      name = "\"order\""
   )
   private List<RegionCourt> childrenCourts = new ArrayList();

   public GddsRegion(Long id, String name, String shortName, GddsRegion parent, Long level) {
      super(id, name, shortName, level);
      this.parent = parent;
   }

   public void addCourtToChildren(GddsCourt court) {
      RegionCourt regionCourt = new RegionCourt(this, court);
      court.setParentRegion(regionCourt);
      this.getChildrenCourts().add(regionCourt);
   }

   public Boolean getOutdated() {
      return this.outdated;
   }

   public GddsRegion getParent() {
      return this.parent;
   }

   public List<RegionCourt> getChildrenCourts() {
      return this.childrenCourts;
   }

   public void setOutdated(final Boolean outdated) {
      this.outdated = outdated;
   }

   public void setParent(final GddsRegion parent) {
      this.parent = parent;
   }

   public void setChildrenCourts(final List<RegionCourt> childrenCourts) {
      this.childrenCourts = childrenCourts;
   }

   public GddsRegion() {
   }
}
