package com.radiant.fileAccess.service;

import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.utils.MinioUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpRequestProxyVisitor implements HttpDataConnectorVisitor<Void> {
   private final HttpServletRequest initialRequest;
   private final HttpServletResponse response;
   private final FileAccessPath fileAccessPath;
   private final String normalizedPath;

   public HttpRequestProxyVisitor(HttpServletRequest initialRequest, HttpServletResponse response, FileAccessPath fileAccessPath, String normalizedPath) {
      this.initialRequest = initialRequest;
      this.response = response;
      this.fileAccessPath = fileAccessPath;
      this.normalizedPath = normalizedPath;
   }

   public Void visit(MinioDataConnector minioDataConnector) throws Exception {
      MinioUtils.proxyRequest(minioDataConnector, this.initialRequest, this.response, this.fileAccessPath, this.normalizedPath);
      return null;
   }
}
