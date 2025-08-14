package com.radiant.dataConnector.domain;

import com.radiant.dataConnector.service.DataConnectorVisitor;
import com.radiant.fileAccess.service.FileDataConnectorVisitor;
import java.nio.file.Path;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(
   name = "local_file_system_data_connector"
)
@Inheritance(
   strategy = InheritanceType.JOINED
)
@PrimaryKeyJoinColumn(
   foreignKey = @ForeignKey(
   name = "local_file_system_data_connector_pk_fk"
)
)
public class LocalFileSystemDataConnector extends FileDataConnector {
   public @NotNull DataConnectorType getType() {
      return DataConnectorType.LOCAL_FILE_SYSTEM;
   }

   public String normalize(Path input) {
      return input.normalize().toString();
   }

   public <T> T accept(FileDataConnectorVisitor<T> visitor) throws Exception {
      return (T)visitor.visit(this);
   }

   public <T> T accept(DataConnectorVisitor<T> visitor) {
      return (T)visitor.visit(this);
   }

   public String toString() {
      return "LocalFileSystemDataConnector(super=" + super.toString() + ")";
   }
}
