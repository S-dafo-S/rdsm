package com.radiant.fileAccess.path.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class DataConnectorFilePathsDto {
   private @NotNull @Valid DataConnectorDto dataConnector;
   private @NotNull List<@Valid FileAccessPathDto> fileAccessPaths;

   public DataConnectorFilePathsDto(DataConnector connector, List<FileAccessPath> paths) {
      this.dataConnector = new DataConnectorDto(connector);
      this.fileAccessPaths = (List)paths.stream().map(FileAccessPathDto::new).collect(Collectors.toList());
   }

   public DataConnectorFilePathsDto(DataConnector connector) {
      this.dataConnector = new DataConnectorDto(connector);
      this.fileAccessPaths = (List)connector.getFileAccessPaths().stream().map(FileAccessPathDto::new).collect(Collectors.toList());
   }

   public DataConnectorDto getDataConnector() {
      return this.dataConnector;
   }

   public List<@Valid FileAccessPathDto> getFileAccessPaths() {
      return this.fileAccessPaths;
   }

   public void setDataConnector(final DataConnectorDto dataConnector) {
      this.dataConnector = dataConnector;
   }

   public void setFileAccessPaths(final List<@Valid FileAccessPathDto> fileAccessPaths) {
      this.fileAccessPaths = fileAccessPaths;
   }

   public String toString() {
      return "DataConnectorFilePathsDto(dataConnector=" + this.getDataConnector() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DataConnectorFilePathsDto)) {
         return false;
      } else {
         DataConnectorFilePathsDto other = (DataConnectorFilePathsDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$dataConnector = this.getDataConnector();
            Object other$dataConnector = other.getDataConnector();
            if (this$dataConnector == null) {
               if (other$dataConnector != null) {
                  return false;
               }
            } else if (!this$dataConnector.equals(other$dataConnector)) {
               return false;
            }

            Object this$fileAccessPaths = this.getFileAccessPaths();
            Object other$fileAccessPaths = other.getFileAccessPaths();
            if (this$fileAccessPaths == null) {
               if (other$fileAccessPaths != null) {
                  return false;
               }
            } else if (!this$fileAccessPaths.equals(other$fileAccessPaths)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof DataConnectorFilePathsDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $dataConnector = this.getDataConnector();
      result = result * 59 + ($dataConnector == null ? 43 : $dataConnector.hashCode());
      Object $fileAccessPaths = this.getFileAccessPaths();
      result = result * 59 + ($fileAccessPaths == null ? 43 : $fileAccessPaths.hashCode());
      return result;
   }

   public DataConnectorFilePathsDto() {
   }
}
