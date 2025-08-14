package com.radiant.account.service;

import com.radiant.account.domain.Contact;
import com.radiant.account.domain.User;
import com.radiant.account.domain.UserRole;
import com.radiant.account.domain.UserStatus;
import com.radiant.account.domain.repository.UserRepository;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Service
@Transactional
public class DssAccountServiceImpl extends AccountServiceImpl {
   private static final Logger LOG = LoggerFactory.getLogger(DssAccountServiceImpl.class);
   @Autowired
   private UserRepository userRepository;
   @Autowired
   private PasswordEncoder passwordEncoder;
   @Value("${dss.sysadmin.login}")
   private String sysadminLogin;

   public void createInitialStructure() {
      if (this.userRepository.findByStatusAndRolesContains(UserStatus.ACTIVE, UserRole.DSS_SYSADMIN).isEmpty()) {
         LOG.info("Creating initial DSS user: {}", this.sysadminLogin);
         User sysadmin = new User(this.sysadminLogin, this.passwordEncoder.encode(this.sysadminLogin), Collections.singleton(UserRole.DSS_SYSADMIN));
         Contact contact = new Contact("System Admin");
         sysadmin.setContact(contact);
         this.userRepository.save(sysadmin);
      }

   }
}
