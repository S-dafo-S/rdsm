package com.radiant.fileAccess.utils;

import com.radiant.dataConnector.SizedInputStream;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.fileAccess.HttpWebDavFailure;
import com.radiant.exception.fileAccess.NoSuchFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;

public class HttpUtils {
   private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);
   private static final String[] PASSTHROUGH_HEADERS = new String[]{"Content-Type", "Content-Disposition", "Content-Length", "Content-Range", "Accept-Ranges"};

   public static void proxyRequest(HttpClient client, URI hostUri, String path, HttpServletRequest initialRequest, HttpServletResponse response) {
      HttpMethod resolvedMethod = HttpMethod.resolve(initialRequest.getMethod());
      if (resolvedMethod != HttpMethod.GET && resolvedMethod != HttpMethod.HEAD) {
         throw new IllegalArgumentException("Invalid resolvedMethod, either GET or HEAD expected");
      } else {
         HttpMethodBase method = (HttpMethodBase)(resolvedMethod == HttpMethod.HEAD ? new HeadMethod(hostUri.toString()) : new GetMethod(hostUri.toString()));
         String rangeHeader = initialRequest.getHeader("Range");
         if (rangeHeader != null) {
            method.addRequestHeader("Range", rangeHeader);
         }

         try {
            client.executeMethod(method);

            for(String headerName : PASSTHROUGH_HEADERS) {
               Header header = method.getResponseHeader(headerName);
               if (header != null) {
                  response.setHeader(header.getName(), header.getValue());
               }
            }

            response.setStatus(method.getStatusCode());
            InputStream responseBodyAsStream = method.getResponseBodyAsStream();
            if (responseBodyAsStream != null) {
               StreamUtils.copy(responseBodyAsStream, response.getOutputStream());
            }
         } catch (IOException e) {
            LOG.error("Failed to proxy {} {} to {}", new Object[]{resolvedMethod, path, hostUri, e});
         }

      }
   }

   public static SizedInputStream getFile(HttpClient client, URI hostUri, NoSuchFileException ex) {
      try {
         GetMethod method = new GetMethod(hostUri.toString());
         client.executeMethod(method);
         if (method.getStatusCode() == 200) {
            return new SizedInputStream(method.getResponseBodyAsStream(), method.getResponseContentLength());
         } else if (method.getStatusCode() == 404) {
            throw ex;
         } else {
            throw new HttpWebDavFailure();
         }
      } catch (IOException e) {
         throw new RdsmIOException(e);
      }
   }
}
