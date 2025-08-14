package com.radiant.applicationRegistry.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
   name = "ip_address"
)
public class IpAddress {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   @Column(
      name = "id",
      updatable = false,
      nullable = false
   )
   private Long id;
   @Column(
      name = "address",
      nullable = false
   )
   private String address;
   @ManyToOne
   @JoinColumn(
      name = "application_registry_id",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "ip_address_application_registry_fk"
)
   )
   private ApplicationRegistry applicationRegistry;

   public IpAddress(String address, ApplicationRegistry applicationRegistry) {
      this.address = address;
      this.applicationRegistry = applicationRegistry;
   }

   public Long getId() {
      return this.id;
   }

   public String getAddress() {
      return this.address;
   }

   public ApplicationRegistry getApplicationRegistry() {
      return this.applicationRegistry;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setAddress(final String address) {
      this.address = address;
   }

   public void setApplicationRegistry(final ApplicationRegistry applicationRegistry) {
      this.applicationRegistry = applicationRegistry;
   }

   public IpAddress() {
   }
}
