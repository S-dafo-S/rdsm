package com.radiant.auth.domain;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicationHeaderRepository extends JpaRepository<ApplicationHeader, Long> {
   @Transactional
   void deleteAllByApplication(ApplicationRegistry application);

   @Transactional
   void deleteAllByExpirationDateBefore(Date date);

   ApplicationHeader getByHeader(String header);
}
