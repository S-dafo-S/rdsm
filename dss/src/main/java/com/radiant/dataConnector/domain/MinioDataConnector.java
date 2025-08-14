package com.radiant.dataConnector.domain;

import com.radiant.dataConnector.service.DataConnectorVisitor;
import com.radiant.ecrypt.EncryptorConverter;
import com.radiant.fileAccess.service.FileDataConnectorVisitor;
import com.radiant.fileAccess.service.HttpDataConnectorVisitor;
import com.radiant.fileAccess.service.S3TestConnectionVisitor;
import java.io.File;
import java.nio.file.Path;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(
   name = "minio_data_connector"
)
@Inheritance(
   strategy = InheritanceType.JOINED
)
@PrimaryKeyJoinColumn(
   foreignKey = @ForeignKey(
   name = "minio_data_connector_pk_fk"
)
)
public class MinioDataConnector extends HttpFileDataConnector implements S3DataConnector {
   @Column(
      name = "endpoint",
      nullable = false
   )
   private String endpoint;
   @Column(
      name = "access_key_id",
      nullable = false
   )
   private String accessKeyId;
   @Convert(
      converter = EncryptorConverter.class
   )
   @Column(
      name = "access_key_secret",
      nullable = false
   )
   private String accessKeySecret;
   @Column(
      name = "bucket_name"
   )
   private String bucketName;

   public DataConnectorType getType() {
      return DataConnectorType.MINIO;
   }

   public <T> T accept(DataConnectorVisitor<T> visitor) {
      return (T)visitor.visit(this);
   }

   public String normalize(Path input) {
      Path firstStep = input.normalize();
      return firstStep.toString().replace(File.separator, "/");
   }

   public <T> T accept(FileDataConnectorVisitor<T> visitor) throws Exception {
      return (T)visitor.visit(this);
   }

   public <T> T accept(HttpDataConnectorVisitor<T> visitor) throws Exception {
      return visitor.visit(this);
   }

   public <T> boolean testConnection(S3TestConnectionVisitor<T> visitor) throws Exception {
      return visitor.checkConnection(this);
   }

   public String getEndpoint() {
      return this.endpoint;
   }

   public String getAccessKeyId() {
      return this.accessKeyId;
   }

   public String getAccessKeySecret() {
      return this.accessKeySecret;
   }

   public String getBucketName() {
      return this.bucketName;
   }

   public void setEndpoint(final String endpoint) {
      this.endpoint = endpoint;
   }

   public void setAccessKeyId(final String accessKeyId) {
      this.accessKeyId = accessKeyId;
   }

   public void setAccessKeySecret(final String accessKeySecret) {
      this.accessKeySecret = accessKeySecret;
   }

   public void setBucketName(final String bucketName) {
      this.bucketName = bucketName;
   }

   public String toString() {
      return "MinioDataConnector(super=" + super.toString() + ", endpoint=" + this.getEndpoint() + ", accessKeyId=" + this.getAccessKeyId() + ", accessKeySecret=" + this.getAccessKeySecret() + ", bucketName=" + this.getBucketName() + ")";
   }
}
