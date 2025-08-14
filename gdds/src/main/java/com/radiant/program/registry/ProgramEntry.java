package com.radiant.program.registry;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProgramEntry {
   @Column(
      name = "service_name",
      nullable = false
   )
   String serviceName;
   @Column(
      name = "class_name",
      nullable = false
   )
   String className;

   public String getServiceName() {
      return this.serviceName;
   }

   public String getClassName() {
      return this.className;
   }

   public void setServiceName(final String serviceName) {
      this.serviceName = serviceName;
   }

   public void setClassName(final String className) {
      this.className = className;
   }
}
