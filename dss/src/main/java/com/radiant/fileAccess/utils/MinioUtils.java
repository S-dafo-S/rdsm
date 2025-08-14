package com.radiant.fileAccess.utils;

import com.radiant.dataConnector.SizedInputStream;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.exception.dataConnector.MinioFileReadException;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

public class MinioUtils {
   private static final Logger LOG = LoggerFactory.getLogger(MinioUtils.class);
   private static final String[] PASSTHROUGH_HEADERS = new String[]{"Content-Disposition", "Content-Length", "Content-Range", "Accept-Ranges"};

   public static boolean bucketExist(MinioDataConnector connector, String bucketName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      try {
         MinioClient client = minioClient(connector, new FileAccessPath());
         return client.bucketExists((BucketExistsArgs)((BucketExistsArgs.Builder)BucketExistsArgs.builder().bucket(bucketName)).build());
      } catch (MinioException exc) {
         LOG.error("Failed to read MinIO file", exc);
         throw new MinioFileReadException(exc);
      }
   }

   public static @NotNull SizedInputStream getFile(MinioDataConnector connector, FileAccessPath fileAccessPath, String objectName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      GetObjectResponse response;
      try {
         MinioClient client = minioClient(connector, fileAccessPath);
         AwsUtils.S3Path s3Path = AwsUtils.getBucketAndPath(connector.getBucketName(), objectName);
         response = client.getObject((GetObjectArgs)((GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(s3Path.getBucketName())).object(s3Path.getObjectPath())).build());
      } catch (MinioException exc) {
         LOG.error("Failed to read MinIO file", exc);
         throw new MinioFileReadException(exc);
      }

      String contentLength = response.headers().get("Content-Length");
      LOG.trace("Response length: {}", contentLength);
      return new SizedInputStream(response, ObjectUtils.isEmpty(contentLength) ? -1L : Long.parseLong(contentLength));
   }

   public static List<String> listFiles(MinioDataConnector connector, FileAccessPath fileAccessPath, String filePrefix) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      List<String> result = new ArrayList();

      try {
         MinioClient client = minioClient(connector, fileAccessPath);
         AwsUtils.S3Path s3Path = AwsUtils.getBucketAndPath(connector.getBucketName(), filePrefix);

         for(Result<Item> object : client.listObjects((ListObjectsArgs)((ListObjectsArgs.Builder)ListObjectsArgs.builder().bucket(s3Path.getBucketName())).prefix(s3Path.getObjectPath()).build())) {
            result.add(((Item)object.get()).objectName());
         }

         return result;
      } catch (MinioException exc) {
         LOG.error("Failed to read MinIO file", exc);
         throw new MinioFileReadException(exc);
      }
   }

   public static void proxyRequest(MinioDataConnector connector, HttpServletRequest initialRequest, HttpServletResponse response, FileAccessPath fileAccessPath, String targetPath) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      try {
         MinioClient client = minioClient(connector, fileAccessPath);
         String rangeHeader = initialRequest.getHeader("Range");
         AwsUtils.S3Path s3Path = AwsUtils.getBucketAndPath(connector.getBucketName(), targetPath);
         GetObjectArgs.Builder argBuilder = (GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(s3Path.getBucketName())).object(s3Path.getObjectPath());
         if (rangeHeader != null) {
            Map<String, String> rangeHeaderMap = new HashMap();
            rangeHeaderMap.put("Range", rangeHeader);
            argBuilder.extraHeaders(rangeHeaderMap);
         }

         GetObjectResponse getResponse = client.getObject((GetObjectArgs)argBuilder.build());

         for(String headerName : PASSTHROUGH_HEADERS) {
            String headerValue = getResponse.headers().get(headerName);
            if (headerValue != null) {
               response.setHeader(headerName, headerValue);
            }
         }

         StreamUtils.copy(getResponse, response.getOutputStream());
      } catch (MinioException exc) {
         LOG.error("Failed to read MinIO file", exc);
         throw new MinioFileReadException(exc);
      }
   }

   private static @NotNull MinioClient minioClient(MinioDataConnector connector, FileAccessPath fileAccessPath) {
      String accessKey = fileAccessPath.getUserId() != null ? fileAccessPath.getUserId() : connector.getAccessKeyId();
      String accessSecret = fileAccessPath.getUserPassword() != null ? fileAccessPath.getUserPassword() : connector.getAccessKeySecret();
      MinioClient minio = MinioClient.builder().endpoint(connector.getEndpoint()).credentials(accessKey, accessSecret).build();
      LOG.info("MinIO client started for endpoint {}", connector.getEndpoint());
      return minio;
   }
}
