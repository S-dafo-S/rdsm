package com.radiant.fileAccess.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.radiant.exception.dataConnector.InsufficientUserPermissionsException;
import com.radiant.exception.dataConnector.MissingBucketNameException;
import com.radiant.exception.dataConnector.UnknownBucketNameException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsUtils {
   private static final Logger LOG = LoggerFactory.getLogger(AwsUtils.class);

   public static S3Path getBucketAndPath(String connectorBucketName, @Nullable String targetPath) {
      int minBucketNameLength = 3;
      if (targetPath != null && targetPath.startsWith("/")) {
         targetPath = targetPath.substring(1);
      }

      if (connectorBucketName == null) {
         if (targetPath != null && targetPath.endsWith("/")) {
            targetPath = StringUtils.substring(targetPath, 0, targetPath.length() - 1);
         }

         if (targetPath != null && targetPath.contains("/")) {
            String[] bucketNameAndPath = targetPath.split("/", 2);
            if (bucketNameAndPath[0].length() >= 3) {
               return AwsUtils.S3Path.builder().bucketName(bucketNameAndPath[0]).objectPath(bucketNameAndPath[1]).build();
            }
         }

         throw new MissingBucketNameException();
      } else {
         return AwsUtils.S3Path.builder().bucketName(connectorBucketName).objectPath(targetPath).build();
      }
   }

   static boolean testS3Connection(String connectorBucketName, AmazonS3 client, String name) throws SdkClientException {
      String testBucketName = connectorBucketName;
      if (StringUtils.isEmpty(connectorBucketName)) {
         testBucketName = String.format("test-fake-bucket-name-%s", UUID.randomUUID());
      }

      Boolean isBucketExists = null;

      try {
         if (StringUtils.isNotEmpty(connectorBucketName)) {
            client.listObjects(new ListObjectsRequest(testBucketName, "", "", "", 0));
            isBucketExists = true;
         } else {
            List<Bucket> buckets = client.listBuckets();
            if (buckets != null) {
               return true;
            }
         }
      } catch (AmazonServiceException e) {
         if (404 == e.getStatusCode()) {
            isBucketExists = false;
         } else {
            if ("SignatureDoesNotMatch".equals(e.getErrorCode()) || "InvalidAccessKeyId".equals(e.getErrorCode()) || "PermanentRedirect".equals(e.getErrorCode())) {
               throw new InsufficientUserPermissionsException(e.getErrorMessage());
            }

            if (403 == e.getStatusCode()) {
               LOG.info("Try to get object");
               if (StringUtils.isEmpty(connectorBucketName)) {
                  throw new InsufficientUserPermissionsException("Access denied to get list of buckets!");
               }

               try {
                  client.getObject(testBucketName, UUID.randomUUID().toString());
                  isBucketExists = true;
               } catch (AmazonServiceException exc1) {
                  if (404 == exc1.getStatusCode()) {
                     isBucketExists = !exc1.getErrorCode().equals("NoSuchBucket");
                  }
               }
            }
         }
      }

      if (isBucketExists != null && !isBucketExists && StringUtils.isNotEmpty(connectorBucketName)) {
         throw new UnknownBucketNameException(connectorBucketName);
      } else {
         return true;
      }
   }

   public static class S3Path {
      private String bucketName;
      private String objectPath;

      S3Path(final String bucketName, final String objectPath) {
         this.bucketName = bucketName;
         this.objectPath = objectPath;
      }

      public static S3PathBuilder builder() {
         return new S3PathBuilder();
      }

      public String getBucketName() {
         return this.bucketName;
      }

      public String getObjectPath() {
         return this.objectPath;
      }

      public static class S3PathBuilder {
         private String bucketName;
         private String objectPath;

         S3PathBuilder() {
         }

         public S3PathBuilder bucketName(final String bucketName) {
            this.bucketName = bucketName;
            return this;
         }

         public S3PathBuilder objectPath(final String objectPath) {
            this.objectPath = objectPath;
            return this;
         }

         public S3Path build() {
            return new S3Path(this.bucketName, this.objectPath);
         }

         public String toString() {
            return "AwsUtils.S3Path.S3PathBuilder(bucketName=" + this.bucketName + ", objectPath=" + this.objectPath + ")";
         }
      }
   }
}
