package com.radiant.fileAccess.path.dto;

import javax.validation.constraints.NotEmpty;

public class FileAccessPathMapping {
   private @NotEmpty String logicalPath;
   private @NotEmpty String physicalPath;

   public String getLogicalPath() {
      return this.logicalPath;
   }

   public String getPhysicalPath() {
      return this.physicalPath;
   }

   public void setLogicalPath(final String logicalPath) {
      this.logicalPath = logicalPath;
   }

   public void setPhysicalPath(final String physicalPath) {
      this.physicalPath = physicalPath;
   }

   public String toString() {
      return "FileAccessPathMapping(logicalPath=" + this.getLogicalPath() + ", physicalPath=" + this.getPhysicalPath() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FileAccessPathMapping)) {
         return false;
      } else {
         FileAccessPathMapping other = (FileAccessPathMapping)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$logicalPath = this.getLogicalPath();
            Object other$logicalPath = other.getLogicalPath();
            if (this$logicalPath == null) {
               if (other$logicalPath != null) {
                  return false;
               }
            } else if (!this$logicalPath.equals(other$logicalPath)) {
               return false;
            }

            Object this$physicalPath = this.getPhysicalPath();
            Object other$physicalPath = other.getPhysicalPath();
            if (this$physicalPath == null) {
               if (other$physicalPath != null) {
                  return false;
               }
            } else if (!this$physicalPath.equals(other$physicalPath)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof FileAccessPathMapping;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $logicalPath = this.getLogicalPath();
      result = result * 59 + ($logicalPath == null ? 43 : $logicalPath.hashCode());
      Object $physicalPath = this.getPhysicalPath();
      result = result * 59 + ($physicalPath == null ? 43 : $physicalPath.hashCode());
      return result;
   }
}
