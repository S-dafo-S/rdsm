package com.radiant;

import com.radiant.config.EchoBeanNameGenerator;
import com.radiant.loader.EchoURLClassLoader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DssApplication {
   private static final Logger LOG = LoggerFactory.getLogger(DssApplication.class);
   private static ConfigurableApplicationContext context;

   public static void main(String[] args) {
      context = initApp().run(args);
   }

   public static void restart() {
      LOG.info("Restarting app...");
      ApplicationArguments args = (ApplicationArguments)context.getBean(ApplicationArguments.class);
      Thread thread = new Thread(() -> {
         context.close();
         context = initApp().run(args.getSourceArgs());
      });
      thread.setDaemon(false);
      thread.start();
   }

   private static SpringApplication initApp() {
      EchoURLClassLoader loader = new EchoURLClassLoader(new URL[0], DssApplication.class.getClassLoader());
      Properties props = new Properties();
      String libraryDir = System.getenv("GDDS_LIB_DIR");
      if (libraryDir == null) {
         try {
            InputStream is = loader.getResourceAsStream("application.properties");
            Throwable var4 = null;

            try {
               props.load(is);
            } catch (Throwable var14) {
               var4 = var14;
               throw var14;
            } finally {
               if (is != null) {
                  if (var4 != null) {
                     try {
                        is.close();
                     } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                     }
                  } else {
                     is.close();
                  }
               }

            }
         } catch (IOException var16) {
            LOG.error("Failed to read application properties");
         }

         libraryDir = props.getProperty("gddslib.dir");
      }

      LOG.info("Starting application with external libs {}", libraryDir);
      loader.init(libraryDir);
      SpringApplication app = new SpringApplication(new Class[]{DssApplication.class});
      app.setResourceLoader(new DefaultResourceLoader(loader));
      app.setDefaultProperties(props);
      app.setBeanNameGenerator(new EchoBeanNameGenerator());
      app.addListeners(new ApplicationListener[]{new SecureProperties()});
      return app;
   }
}
