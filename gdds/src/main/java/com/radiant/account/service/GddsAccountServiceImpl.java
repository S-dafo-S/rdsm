package com.radiant.account.service;

import com.radiant.account.domain.Contact;
import com.radiant.account.domain.User;
import com.radiant.account.domain.UserRole;
import com.radiant.account.domain.UserStatus;
import com.radiant.account.domain.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Primary
@Service
@Transactional
public class GddsAccountServiceImpl extends AccountServiceImpl implements GddsAccountService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsAccountServiceImpl.class);
   @Autowired
   private UserRepository userRepository;
   @Value("${gdds.sysadmin.login}")
   private String sysadminLogin;
   @Autowired
   private PasswordEncoder passwordEncoder;

   public void createInitialStructure() {
      if (this.userRepository.findByStatusAndRolesContains(UserStatus.ACTIVE, UserRole.GDDS_SYSADMIN).isEmpty()) {
         LOG.info("Creating initial GDDS user: {}", this.sysadminLogin);
         User sysadmin = new User(this.sysadminLogin, this.passwordEncoder.encode(this.sysadminLogin), Collections.singleton(UserRole.GDDS_SYSADMIN));
         Contact contact = new Contact("System Admin");
         sysadmin.setContact(contact);
         this.userRepository.save(sysadmin);
      }

   }

   public User getGddsSystemAdmin() {
      List<User> admins = this.userRepository.findByStatusAndRolesContains(UserStatus.ACTIVE, UserRole.GDDS_SYSADMIN);
      Assert.notEmpty(admins, "There must at least one system admin");
      return (User)admins.get(0);
   }
}
