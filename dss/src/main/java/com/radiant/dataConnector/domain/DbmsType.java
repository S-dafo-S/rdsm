package com.radiant.dataConnector.domain;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(
   name = "dbms_type"
)
public class DbmsType {
   @Id
   @Enumerated(EnumType.STRING)
   @Column(
      name = "type",
      nullable = false
   )
   private @NotNull DataConnectorType type;
   @Column(
      name = "display_name",
      nullable = false
   )
   private @NotNull String displayName;
   @Column(
      name = "driver_class",
      nullable = false
   )
   private @NotNull String driverClass;
   @Column(
      name = "jdbc_url_format",
      length = 1024,
      nullable = false
   )
   private @NotNull String jdbcUrlFormat;
   @Column(
      name = "driver_cp",
      length = 1024
   )
   private String driverCP;
   @Nullable
   @Column(
      name = "default_port"
   )
   private Integer defaultPort;

   public DataConnectorType getType() {
      return this.type;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDriverClass() {
      return this.driverClass;
   }

   public String getJdbcUrlFormat() {
      return this.jdbcUrlFormat;
   }

   public String getDriverCP() {
      return this.driverCP;
   }

   @Nullable
   public Integer getDefaultPort() {
      return this.defaultPort;
   }

   public void setType(final DataConnectorType type) {
      this.type = type;
   }

   public void setDisplayName(final String displayName) {
      this.displayName = displayName;
   }

   public void setDriverClass(final String driverClass) {
      this.driverClass = driverClass;
   }

   public void setJdbcUrlFormat(final String jdbcUrlFormat) {
      this.jdbcUrlFormat = jdbcUrlFormat;
   }

   public void setDriverCP(final String driverCP) {
      this.driverCP = driverCP;
   }

   public void setDefaultPort(@Nullable final Integer defaultPort) {
      this.defaultPort = defaultPort;
   }
}
