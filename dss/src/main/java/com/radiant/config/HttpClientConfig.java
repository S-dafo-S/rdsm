package com.radiant.config;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {
   @Bean
   public CloseableHttpClient getHttpClient() throws GeneralSecurityException {
      SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((KeyStore)null, new TrustSelfSignedStrategy()).build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
      return HttpClients.custom().setSSLSocketFactory(sslsf).build();
   }
}
