package com.radiant.fileAccess.path.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.springframework.web.util.UriComponentsBuilder;

@JsonInclude(Include.NON_NULL)
public class FileAccessApiDto {
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private @NotNull FileAccessPathDto fileAccessPath;
   @Nullable
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private String qnodeFilesApiUrl;
   @Nullable
   @JsonProperty(
      access = Access.READ_ONLY
   )
   private String dnodeFilesApiUrl;

   public FileAccessApiDto(@NotNull FileAccessPath path, @Nullable String qnodeBaseUrl, @Nullable String dnodeBaseUrl) {
      this.fileAccessPath = new FileAccessPathDto(path);
      if (qnodeBaseUrl != null) {
         this.qnodeFilesApiUrl = UriComponentsBuilder.fromHttpUrl(qnodeBaseUrl).path("/api/v1/readfile").build().toUriString();
      }

      if (dnodeBaseUrl != null) {
         this.dnodeFilesApiUrl = UriComponentsBuilder.fromHttpUrl(dnodeBaseUrl).path("/api/v1/readfile").build().toUriString();
      }

   }

   public FileAccessPathDto getFileAccessPath() {
      return this.fileAccessPath;
   }

   @Nullable
   public String getQnodeFilesApiUrl() {
      return this.qnodeFilesApiUrl;
   }

   @Nullable
   public String getDnodeFilesApiUrl() {
      return this.dnodeFilesApiUrl;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessApiDto)) {
         return false;
      } else {
         FileAccessApiDto other = (FileAccessApiDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$fileAccessPath = this.getFileAccessPath();
            Object other$fileAccessPath = other.getFileAccessPath();
            if (this$fileAccessPath == null) {
               if (other$fileAccessPath != null) {
                  return false;
               }
            } else if (!this$fileAccessPath.equals(other$fileAccessPath)) {
               return false;
            }

            Object this$qnodeFilesApiUrl = this.getQnodeFilesApiUrl();
            Object other$qnodeFilesApiUrl = other.getQnodeFilesApiUrl();
            if (this$qnodeFilesApiUrl == null) {
               if (other$qnodeFilesApiUrl != null) {
                  return false;
               }
            } else if (!this$qnodeFilesApiUrl.equals(other$qnodeFilesApiUrl)) {
               return false;
            }

            Object this$dnodeFilesApiUrl = this.getDnodeFilesApiUrl();
            Object other$dnodeFilesApiUrl = other.getDnodeFilesApiUrl();
            if (this$dnodeFilesApiUrl == null) {
               if (other$dnodeFilesApiUrl != null) {
                  return false;
               }
            } else if (!this$dnodeFilesApiUrl.equals(other$dnodeFilesApiUrl)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessApiDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $fileAccessPath = this.getFileAccessPath();
      result = result * 59 + ($fileAccessPath == null ? 43 : $fileAccessPath.hashCode());
      Object $qnodeFilesApiUrl = this.getQnodeFilesApiUrl();
      result = result * 59 + ($qnodeFilesApiUrl == null ? 43 : $qnodeFilesApiUrl.hashCode());
      Object $dnodeFilesApiUrl = this.getDnodeFilesApiUrl();
      result = result * 59 + ($dnodeFilesApiUrl == null ? 43 : $dnodeFilesApiUrl.hashCode());
      return result;
   }

   public FileAccessApiDto() {
   }

   public String toString() {
      return "FileAccessApiDto(fileAccessPath=" + this.getFileAccessPath() + ", qnodeFilesApiUrl=" + this.getQnodeFilesApiUrl() + ", dnodeFilesApiUrl=" + this.getDnodeFilesApiUrl() + ")";
   }
}
