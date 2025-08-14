package com.radiant.query.service;

import com.radiant.loader.JarContentLoader;
import com.radiant.plugin.FileAccessAdapter;
import com.radiant.plugin.QueryAdapter;
import com.radiant.query.registry.JarEntryDto;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginJarContentLoader extends JarContentLoader {
   private static final Logger LOG = LoggerFactory.getLogger(PluginJarContentLoader.class);

   public PluginJarContentLoader(ClassLoader parent) {
      super(parent);
   }

   public List<JarEntryDto> findPluginServiceClasses() {
      return (List)this.classes.keySet().stream().map((name) -> {
         try {
            LOG.trace("Loader found class {}", name);
            return this.loadClass(name);
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
         }
      }).filter((cls) -> QueryAdapter.class.isAssignableFrom(cls) || FileAccessAdapter.class.isAssignableFrom(cls)).map((klass) -> {
         String rawServiceName = this.getRawServiceName(klass);
         if (rawServiceName == null) {
            return null;
         } else {
            JarEntryDto entry = new JarEntryDto();
            entry.setClassName(klass.getName());
            entry.setServiceName(rawServiceName);
            return entry;
         }
      }).filter(Objects::nonNull).collect(Collectors.toList());
   }
}
