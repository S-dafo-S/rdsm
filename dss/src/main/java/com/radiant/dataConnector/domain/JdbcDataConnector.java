package com.radiant.dataConnector.domain;

import com.radiant.dataConnector.service.DataConnectorVisitor;
import com.radiant.ecrypt.EncryptorConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(
   name = "jdbc_data_connector"
)
@Inheritance(
   strategy = InheritanceType.JOINED
)
@PrimaryKeyJoinColumn(
   foreignKey = @ForeignKey(
   name = "jdbc_data_connector_pk_fk"
)
)
public class JdbcDataConnector extends DataConnector {
   @ManyToOne
   @JoinColumn(
      name = "dbms_type",
      nullable = false,
      foreignKey = @ForeignKey(
   name = "jdbc_data_connector_type_fk"
)
   )
   private DbmsType dbmsType;
   @Column(
      name = "hostname",
      nullable = false
   )
   private String hostname;
   @Column(
      name = "port",
      nullable = false
   )
   private Integer port;
   @Column(
      name = "user_id",
      nullable = false
   )
   private String userId;
   @Convert(
      converter = EncryptorConverter.class
   )
   @Column(
      name = "user_password",
      nullable = false
   )
   private String userPassword;
   @Column(
      name = "db_name",
      nullable = false,
      length = 128
   )
   private String dbName;
   @Column(
      name = "custom_jdbc_url",
      length = 1024
   )
   private String customJdbcUrl;
   @Column(
      name = "db_version"
   )
   private String dbVersion;

   public @NotNull DataConnectorType getType() {
      return this.dbmsType.getType();
   }

   public <T> T accept(DataConnectorVisitor<T> visitor) {
      return (T)visitor.visit(this);
   }

   public DbmsType getDbmsType() {
      return this.dbmsType;
   }

   public String getHostname() {
      return this.hostname;
   }

   public Integer getPort() {
      return this.port;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getUserPassword() {
      return this.userPassword;
   }

   public String getDbName() {
      return this.dbName;
   }

   public String getCustomJdbcUrl() {
      return this.customJdbcUrl;
   }

   public String getDbVersion() {
      return this.dbVersion;
   }

   public void setDbmsType(final DbmsType dbmsType) {
      this.dbmsType = dbmsType;
   }

   public void setHostname(final String hostname) {
      this.hostname = hostname;
   }

   public void setPort(final Integer port) {
      this.port = port;
   }

   public void setUserId(final String userId) {
      this.userId = userId;
   }

   public void setUserPassword(final String userPassword) {
      this.userPassword = userPassword;
   }

   public void setDbName(final String dbName) {
      this.dbName = dbName;
   }

   public void setCustomJdbcUrl(final String customJdbcUrl) {
      this.customJdbcUrl = customJdbcUrl;
   }

   public void setDbVersion(final String dbVersion) {
      this.dbVersion = dbVersion;
   }

   public String toString() {
      return "JdbcDataConnector(super=" + super.toString() + ", hostname=" + this.getHostname() + ", port=" + this.getPort() + ", userId=" + this.getUserId() + ", dbName=" + this.getDbName() + ", customJdbcUrl=" + this.getCustomJdbcUrl() + ", dbVersion=" + this.getDbVersion() + ")";
   }
}
