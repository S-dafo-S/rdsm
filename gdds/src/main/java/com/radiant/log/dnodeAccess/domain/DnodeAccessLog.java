package com.radiant.log.dnodeAccess.domain;

import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.util.DateUtils;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
   name = "dnode_access_log"
)
public class DnodeAccessLog {
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
      name = "dnode",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "dnode_access_log_dnode_fk"
)
   )
   private DNode dnode;
   @Column(
      name = "url",
      length = 10240,
      nullable = false
   )
   private String url;
   @Column(
      name = "time",
      nullable = false
   )
   @Temporal(TemporalType.TIMESTAMP)
   private Date time;
   @ColumnDefault("true")
   @Column(
      name = "success",
      nullable = false
   )
   private Boolean success = true;
   @Column(
      name = "message",
      length = 10240
   )
   private String message;

   public Date getTime() {
      return DateUtils.cloneDate(this.time);
   }

   public void setTime(Date time) {
      this.time = DateUtils.cloneDate(time);
   }

   public Long getId() {
      return this.id;
   }

   public DNode getDnode() {
      return this.dnode;
   }

   public String getUrl() {
      return this.url;
   }

   public Boolean getSuccess() {
      return this.success;
   }

   public String getMessage() {
      return this.message;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setDnode(final DNode dnode) {
      this.dnode = dnode;
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public void setSuccess(final Boolean success) {
      this.success = success;
   }

   public void setMessage(final String message) {
      this.message = message;
   }
}
