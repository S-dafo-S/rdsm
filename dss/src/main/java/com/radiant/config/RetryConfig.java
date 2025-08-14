package com.radiant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfig {
   @Bean
   public RetryTemplate retryTemplate() {
      ExponentialRandomBackOffPolicy expRandomBackOffPolicy = new ExponentialRandomBackOffPolicy();
      expRandomBackOffPolicy.setInitialInterval(2000L);
      expRandomBackOffPolicy.setMaxInterval(3600000L);
      expRandomBackOffPolicy.setMultiplier((double)10.0F);
      RetryTemplate retryTemplate = new RetryTemplate();
      retryTemplate.setBackOffPolicy(expRandomBackOffPolicy);
      SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
      retryPolicy.setMaxAttempts(3);
      return retryTemplate;
   }
}
