package com.radiant.config;

import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class I18nConfig implements WebMvcConfigurer {
   @Bean
   public ResourceBundleMessageSource messageSource() {
      ResourceBundleMessageSource source = new ResourceBundleMessageSource();
      source.setBasenames(new String[]{"messages"});
      source.setDefaultEncoding("UTF-8");
      source.setUseCodeAsDefaultMessage(true);
      return source;
   }

   @Bean
   public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
      return (jacksonObjectMapperBuilder) -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
   }
}
