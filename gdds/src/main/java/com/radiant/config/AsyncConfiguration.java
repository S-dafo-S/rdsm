package com.radiant.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfiguration implements AsyncConfigurer {
   private static final Logger LOG = LoggerFactory.getLogger(AsyncConfiguration.class);
   private final Integer CORE_POOL_SIZE = 2;
   private final Integer MAX_POOL_SIZE = 10;
   private final Integer QUEUE_CAPACITY = 20;

   public Executor getAsyncExecutor() {
      LOG.info("Init async executor");
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(this.CORE_POOL_SIZE);
      executor.setMaxPoolSize(this.MAX_POOL_SIZE);
      executor.setQueueCapacity(this.QUEUE_CAPACITY);
      executor.setThreadNamePrefix("EchoExecutor-");
      executor.initialize();
      return executor;
   }

   public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
      return new SimpleAsyncUncaughtExceptionHandler();
   }
}
