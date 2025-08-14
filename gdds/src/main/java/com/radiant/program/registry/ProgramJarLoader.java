package com.radiant.program.registry;

import com.radiant.loader.JarContentLoader;
import com.radiant.program.DnodeProgramAdapter;
import com.radiant.program.QnodeProgramAdapter;
import com.radiant.program.dto.RdsmProgram;
import com.radiant.query.registry.JarEntryDto;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramJarLoader extends JarContentLoader {
   private static final Logger LOG = LoggerFactory.getLogger(ProgramJarLoader.class);

   public ProgramJarLoader(ClassLoader parent) {
      super(parent);
   }

   public List<JarEntryDto> findProgramServiceClasses() {
      return (List)this.classes.keySet().stream().map((name) -> {
         try {
            LOG.trace("Loader found class {}", name);
            return this.loadClass(name);
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
         }
      }).filter((cls) -> QnodeProgramAdapter.class.isAssignableFrom(cls) || DnodeProgramAdapter.class.isAssignableFrom(cls)).map((klass) -> {
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

   public RdsmProgram findProgramAnnotation() {
      Set<? extends Class<?>> annotatedClasses = (Set)this.classes.keySet().stream().map((name) -> {
         try {
            LOG.trace("Loader found class {}", name);
            return this.loadClass(name);
         } catch (ClassNotFoundException e) {
            LOG.error("Expected class not found in the program");
            throw new RuntimeException(e);
         }
      }).filter((cls) -> QnodeProgramAdapter.class.isAssignableFrom(cls) || DnodeProgramAdapter.class.isAssignableFrom(cls)).collect(Collectors.toSet());
      return this.findEchoProgramAnnotation(annotatedClasses);
   }

   private RdsmProgram findEchoProgramAnnotation(Set<? extends Class<?>> klasses) {
      Stream<Class<?>> classesStream = klasses.stream();
      QnodeProgramAdapter.class.getClass();
      Optional<? extends Class<?>> gddsAnnotatedClass = classesStream.filter(QnodeProgramAdapter.class::isAssignableFrom).findAny();
      if (gddsAnnotatedClass.isPresent()) {
         RdsmProgram annotation = (RdsmProgram)((Class)gddsAnnotatedClass.get()).getAnnotation(RdsmProgram.class);
         LOG.trace("Echo program annotation detected {}", annotation);
         return annotation;
      } else {
         classesStream = klasses.stream();
         DnodeProgramAdapter.class.getClass();
         Optional<? extends Class<?>> dssAnnotatedClass = classesStream.filter(DnodeProgramAdapter.class::isAssignableFrom).findAny();
         if (dssAnnotatedClass.isPresent()) {
            RdsmProgram annotation = (RdsmProgram)((Class)dssAnnotatedClass.get()).getAnnotation(RdsmProgram.class);
            LOG.trace("Echo program annotation detected {}", annotation);
            return annotation;
         } else {
            throw new RuntimeException("EchoProgram annotation is not found");
         }
      }
   }
}
