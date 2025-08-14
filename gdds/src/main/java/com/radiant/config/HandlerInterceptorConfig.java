package com.radiant.config;

import com.radiant.log.access.service.ApiCallHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HandlerInterceptorConfig implements WebMvcConfigurer {
   @Autowired
   private ApiCallHandlerInterceptor apiCallHandlerInterceptor;

   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(this.apiCallHandlerInterceptor);
   }
}
