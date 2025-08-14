package com.radiant.court.domain;

import com.radiant.dataSharingSystem.domain.DNode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "court_host",
   uniqueConstraints = {@UniqueConstraint(
   name = "court_host_dss_uniq",
   columnNames = {"court", "dss"}
)}
)
public class GddsHostedCourt extends HostedCourt {
   @ManyToOne
   @JoinColumn(
      name = "court",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "court_host_court_fk"
)
   )
   private GddsCourt court;
   @ManyToOne
   @JoinColumn(
      name = "dss",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "court_host_dss_fk"
)
   )
   private DNode dss;

   public GddsCourt getCourt() {
      return this.court;
   }

   public DNode getDss() {
      return this.dss;
   }

   public void setCourt(final GddsCourt court) {
      this.court = court;
   }

   public void setDss(final DNode dss) {
      this.dss = dss;
   }
}
