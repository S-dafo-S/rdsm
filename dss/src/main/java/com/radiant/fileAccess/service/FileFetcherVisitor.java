package com.radiant.fileAccess.service;

import com.radiant.dataConnector.SizedInputStream;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.utils.LocalFileUtils;
import com.radiant.fileAccess.utils.MinioUtils;

class FileFetcherVisitor implements FileDataConnectorVisitor<SizedInputStream> {
   private final FileAccessPath fileAccessPath;
   private final String path;
   private final String customIp;
   private final String fullLogicalPath;
   private final long offset;
   private final int bufferSize;

   FileFetcherVisitor(FileAccessPath fileAccessPath, String path, String customIp, String fullLogicalPath) {
      this.fileAccessPath = fileAccessPath;
      this.path = path;
      this.customIp = customIp;
      this.fullLogicalPath = fullLogicalPath;
      this.offset = 0L;
      this.bufferSize = 0;
   }

   FileFetcherVisitor(FileAccessPath fileAccessPath, String path, String customIp, String fullLogicalPath, long offset, int bufferSize) {
      this.fileAccessPath = fileAccessPath;
      this.path = path;
      this.customIp = customIp;
      this.fullLogicalPath = fullLogicalPath;
      this.offset = offset;
      this.bufferSize = bufferSize;
   }

   public SizedInputStream visit(LocalFileSystemDataConnector localFSDataConnector) throws Exception {
      return LocalFileUtils.getFile(localFSDataConnector, this.fileAccessPath, this.path);
   }

   public SizedInputStream visit(MinioDataConnector minioDataConnector) throws Exception {
      return MinioUtils.getFile(minioDataConnector, this.fileAccessPath, this.path);
   }
}
