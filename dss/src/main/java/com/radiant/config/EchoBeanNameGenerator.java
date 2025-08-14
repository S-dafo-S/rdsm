package com.radiant.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;

public class EchoBeanNameGenerator extends AnnotationBeanNameGenerator {
   private static final Logger LOG = LoggerFactory.getLogger(EchoBeanNameGenerator.class);
   private static final String QUERY_PLUGIN_INTERFACE = "com.radiant.plugin.QueryAdapter";
   private static final String FILE_ACCESS_PLUGIN_INTERFACE = "com.radiant.plugin.FileAccessAdapter";

   @NonNull
   public String generateBeanName(@NonNull BeanDefinition definition, @NonNull BeanDefinitionRegistry registry) {
      if (!this.isQueryPlugin(definition) && !this.isFileAccessPlugin(definition)) {
         return super.generateBeanName(definition, registry);
      } else {
         LOG.info("Defining custom bean name for def: {}", definition.getBeanClassName());
         return super.generateBeanName(definition, registry).concat(this.generatePostfix((ScannedGenericBeanDefinition)definition));
      }
   }

   private boolean isQueryPlugin(BeanDefinition definition) {
      return definition instanceof ScannedGenericBeanDefinition && Arrays.asList(((ScannedGenericBeanDefinition)definition).getMetadata().getInterfaceNames()).contains("com.radiant.plugin.QueryAdapter");
   }

   private boolean isFileAccessPlugin(BeanDefinition definition) {
      return definition instanceof ScannedGenericBeanDefinition && Arrays.asList(((ScannedGenericBeanDefinition)definition).getMetadata().getInterfaceNames()).contains("com.radiant.plugin.FileAccessAdapter");
   }

   private String generatePostfix(ScannedGenericBeanDefinition pluginDefinition) {
      Resource resource = pluginDefinition.getResource();
      if (!(resource instanceof UrlResource)) {
         LOG.error("Failed to generate bean postfix, fallback to default name");
         return "";
      } else {
         try {
            String absPath = resource.getURL().getPath();
            int extensionIndex = absPath.indexOf(".jar!");
            if (extensionIndex < 0) {
               LOG.warn("Failed to parse jar name for custom query plugin bean name {}", pluginDefinition.getBeanClassName());
               return "";
            } else {
               File file = new File(absPath.substring(0, extensionIndex));
               return "_".concat(file.getName());
            }
         } catch (IOException e) {
            LOG.error("Failed to generate query plugin custom name", e);
            return "";
         }
      }
   }
}
