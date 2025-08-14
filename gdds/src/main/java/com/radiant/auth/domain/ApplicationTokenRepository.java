package com.radiant.auth.domain;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicationTokenRepository extends JpaRepository<ApplicationToken, Long> {
   Optional<ApplicationToken> findFirstByApplicationAppIdAndToken(String appId, String token);

   List<ApplicationToken> findByApplicationAppIdAndToken(String appId, String token);

   void deleteAllByApplication(ApplicationRegistry application);

   @Modifying
   @Transactional
   @Query("DELETE FROM ApplicationToken t WHERE t.application = :app AND (t.lastResponseTime < :expiration OR (t.lastResponseTime IS NULL and t.lastQueryTime < :expiration) OR (t.lastQueryTime IS NULL AND t.authenticationTime < :expiration))")
   void deleteExpired(ApplicationRegistry app, Date expiration);
}
