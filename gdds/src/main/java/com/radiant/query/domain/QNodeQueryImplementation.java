package com.radiant.query.domain;

import com.radiant.dataSharingSystem.domain.DNode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
   name = "query_implementation"
)
public class QNodeQueryImplementation {
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
      name = "query_id",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "query_implementation_query_fk"
)
   )
   private GddsQuery query;
   @ManyToOne
   @JoinColumn(
      name = "dnode",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "query_impl_dnode_fk"
)
   )
   private DNode dnode;
   @ColumnDefault("false")
   @Column(
      name = "implemented",
      nullable = false
   )
   private Boolean implemented = false;

   public Long getId() {
      return this.id;
   }

   public GddsQuery getQuery() {
      return this.query;
   }

   public DNode getDnode() {
      return this.dnode;
   }

   public Boolean getImplemented() {
      return this.implemented;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setQuery(final GddsQuery query) {
      this.query = query;
   }

   public void setDnode(final DNode dnode) {
      this.dnode = dnode;
   }

   public void setImplemented(final Boolean implemented) {
      this.implemented = implemented;
   }
}
