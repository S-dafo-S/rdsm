package com.radiant.fileAccess.service;

import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.exception.dataConnector.UnknownBucketNameException;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.utils.MinioUtils;
import org.apache.commons.lang3.StringUtils;

public class S3TestConnectionVisitor<T> {
   private final DataConnectorDto request;
   private final FileAccessPath fileAccessPath;

   public S3TestConnectionVisitor(DataConnectorDto request, FileAccessPath fileAccessPath) {
      this.request = request;
      this.fileAccessPath = fileAccessPath;
   }

   public boolean checkConnection(MinioDataConnector minioDataConnector) throws Exception {
      if (StringUtils.isEmpty(this.request.getBucketName())) {
         MinioUtils.bucketExist(minioDataConnector, "test-fake-bucket-name");
         return true;
      } else if (MinioUtils.bucketExist(minioDataConnector, this.request.getBucketName())) {
         return true;
      } else {
         throw new UnknownBucketNameException(this.request.getBucketName());
      }
   }
}
