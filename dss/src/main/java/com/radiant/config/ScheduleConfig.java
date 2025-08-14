package com.radiant.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {
   static final int POOL_SIZE = 4;

   public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
      taskRegistrar.setScheduler(this.taskExecutor());
   }

   @Bean(
      destroyMethod = "shutdown"
   )
   public Executor taskExecutor() {
      return Executors.newScheduledThreadPool(4);
   }

   @Bean
   public TaskScheduler scheduler() {
      return new ConcurrentTaskScheduler();
   }
}
