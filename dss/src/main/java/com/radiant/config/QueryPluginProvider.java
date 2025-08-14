package com.radiant.config;

import com.radiant.exception.query.QueryPluginExecutionException;
import com.radiant.plugin.FileAccessAdapter;
import com.radiant.plugin.GlobalVariables;
import com.radiant.plugin.QueryAdapter;
import com.radiant.plugin.dto.QueryRequest;
import com.radiant.plugin.dto.QueryResponse;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

@Service
public class QueryPluginProvider {
   private static final Logger LOG = LoggerFactory.getLogger(QueryPluginProvider.class);
   @Autowired
   private ResourceLoader resourceLoader;
   private Map<String, QueryAdapter> queryPlugins = new LinkedCaseInsensitiveMap();
   private Map<String, FileAccessAdapter> fileAccessPlugins = new LinkedCaseInsensitiveMap();

   @Autowired
   public QueryPluginProvider(ListableBeanFactory beanFactory) {
      String[] names = beanFactory.getBeanNamesForType(QueryAdapter.class);

      for(String name : names) {
         this.queryPlugins.put(name, beanFactory.getBean(name, QueryAdapter.class));
         LOG.info("Registered query plugin: {}", name);
      }

      String[] faNames = beanFactory.getBeanNamesForType(FileAccessAdapter.class);

      for(String name : faNames) {
         this.fileAccessPlugins.put(name, beanFactory.getBean(name, FileAccessAdapter.class));
         LOG.info("Registered file access plugin: {}", name);
      }

   }

   public boolean isExist(String pluginName) {
      return this.queryPlugins.containsKey(pluginName) || this.fileAccessPlugins.containsKey(pluginName);
   }

   public List<Map.Entry<String, QueryAdapter>> getPluginsByNamePrefix(String namePrefix) {
      return (List)this.queryPlugins.entrySet().stream().filter((entry) -> ((String)entry.getKey()).toLowerCase().startsWith(namePrefix.toLowerCase())).collect(Collectors.toList());
   }

   public List<Map.Entry<String, FileAccessAdapter>> getFileAccessPluginsByNamePrefix(String namePrefix) {
      return (List)this.fileAccessPlugins.entrySet().stream().filter((entry) -> ((String)entry.getKey()).toLowerCase().startsWith(namePrefix.toLowerCase())).collect(Collectors.toList());
   }

   public Object execute(@NotNull String beanName, GlobalVariables globalVariables, Map<String, String> queryArgs, HttpServletResponse servletResponse) {
      try {
         if (this.queryPlugins.containsKey(beanName)) {
            QueryAdapter plugin = (QueryAdapter)this.queryPlugins.get(beanName);
            LOG.info("Query plugin is ok");
            return plugin.get(globalVariables, queryArgs);
         } else if (this.fileAccessPlugins.containsKey(beanName)) {
            FileAccessAdapter plugin = (FileAccessAdapter)this.fileAccessPlugins.get(beanName);
            LOG.info("File Access plugin is ok");
            return plugin.get(globalVariables, queryArgs, new QueryRequest(), new QueryResponse(servletResponse));
         } else {
            throw new RuntimeException("Plugin not found");
         }
      } catch (Exception exception) {
         throw new QueryPluginExecutionException(exception);
      }
   }

   public void unloadPlugin(String jarToUnload, String queryImplName) throws IOException {
      ClassLoader cl = this.resourceLoader.getClassLoader();
      if (cl instanceof URLClassLoader) {
         ((URLClassLoader)cl).close();
         Path jar = Paths.get(jarToUnload);

         try {
            Files.delete(jar);
         } catch (NoSuchFileException var6) {
            LOG.info("File not found for query impl {} while replacing", queryImplName);
         } catch (IOException e) {
            LOG.error("Failed to delete query impl file {}", jar.getFileName(), e);
         }
      } else {
         LOG.warn("Unexpected resource classloader, impl file wasn't unloaded");
      }

   }
}
