package com.radiant;

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

@EnableAsync
@SpringBootApplication
public class GddsApplication {
   private static final Logger LOG = LoggerFactory.getLogger(GddsApplication.class);
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
      EchoURLClassLoader loader = new EchoURLClassLoader(new URL[0], GddsApplication.class.getClassLoader());
      Properties props = new Properties();
      String libraryDir = System.getenv("GDDS_LIB_DIR");
      if (libraryDir == null) {
         try (InputStream is = loader.getResourceAsStream("application.properties")) {
            props.load(is);
         } catch (IOException e) {
            LOG.error("Failed to read application properties", e);
            return new SpringApplication(new Class[]{GddsApplication.class});
         }
         libraryDir = props.getProperty("gddslib.dir");
      }

      LOG.info("Starting application with external libs {}", libraryDir);
      loader.init(libraryDir);
      SpringApplication app = new SpringApplication(new Class[]{GddsApplication.class});
      app.setResourceLoader(new DefaultResourceLoader(loader));
      app.addListeners(new ApplicationListener[]{new SecureProperties()});
      return app;
   }
}
