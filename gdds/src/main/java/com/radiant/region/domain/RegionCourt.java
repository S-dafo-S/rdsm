package com.radiant.region.domain;

import com.radiant.court.domain.GddsCourt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "region_court",
   uniqueConstraints = {@UniqueConstraint(
   name = "region_court_court_uniq",
   columnNames = {"court"}
)}
)
public class RegionCourt {
   @EmbeddedId
   private CompositeKey id;
   @MapsId("regionId")
   @ManyToOne
   @JoinColumn(
      name = "region",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "region_court_region_fk"
)
   )
   private GddsRegion region;
   @MapsId("courtId")
   @OneToOne
   @JoinColumn(
      name = "court",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "region_court_court_fk"
)
   )
   private GddsCourt court;
   @Column(
      name = "\"order\""
   )
   private Integer order;

   public RegionCourt(GddsRegion region, GddsCourt court) {
      this.id = new CompositeKey(region, court);
      this.region = region;
      this.court = court;
   }

   public CompositeKey getId() {
      return this.id;
   }

   public GddsRegion getRegion() {
      return this.region;
   }

   public GddsCourt getCourt() {
      return this.court;
   }

   public void setId(final CompositeKey id) {
      this.id = id;
   }

   public void setRegion(final GddsRegion region) {
      this.region = region;
   }

   public void setCourt(final GddsCourt court) {
      this.court = court;
   }

   public void setOrder(final Integer order) {
      this.order = order;
   }

   public RegionCourt() {
   }

   public Integer getOrder() {
      return this.order;
   }

   @Embeddable
   static class CompositeKey implements Serializable {
      private Long regionId;
      private Long courtId;

      CompositeKey(GddsRegion region, GddsCourt court) {
         this.regionId = region.getId();
         this.courtId = court.getId();
      }

      public CompositeKey() {
      }

      public Long getRegionId() {
         return this.regionId;
      }

      public Long getCourtId() {
         return this.courtId;
      }

      public void setRegionId(final Long regionId) {
         this.regionId = regionId;
      }

      public void setCourtId(final Long courtId) {
         this.courtId = courtId;
      }
   }
}
