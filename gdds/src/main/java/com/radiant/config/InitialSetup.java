package com.radiant.config;

import com.radiant.account.service.AccountService;
import com.radiant.court.service.GddsCourtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitialSetup {
   private static final Logger LOG = LoggerFactory.getLogger(InitialSetup.class);
   @Autowired
   private AccountService accountService;
   @Autowired
   private GddsCourtService courtService;

   @Bean({"initialStructure"})
   protected Object initialStructure() {
      LOG.info("Initializing structure...");
      this.accountService.createInitialStructure();
      this.courtService.createInitialStructure();
      return this;
   }
}
