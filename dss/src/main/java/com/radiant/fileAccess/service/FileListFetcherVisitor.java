package com.radiant.fileAccess.service;

import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.utils.LocalFileUtils;
import com.radiant.fileAccess.utils.MinioUtils;
import java.util.List;

public class FileListFetcherVisitor implements FileDataConnectorVisitor<List<String>> {
   private FileAccessPath fileAccessPath;
   private String path;

   public FileListFetcherVisitor(FileAccessPath fileAccessPath, String path) {
      this.fileAccessPath = fileAccessPath;
      this.path = path;
   }

   public List<String> visit(LocalFileSystemDataConnector localFSDataConnector) throws Exception {
      return LocalFileUtils.listFiles(localFSDataConnector, this.fileAccessPath, this.path);
   }

   public List<String> visit(MinioDataConnector minioDataConnector) throws Exception {
      return MinioUtils.listFiles(minioDataConnector, this.fileAccessPath, this.path);
   }
}
