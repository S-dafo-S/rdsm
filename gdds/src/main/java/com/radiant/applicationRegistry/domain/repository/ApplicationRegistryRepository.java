package com.radiant.applicationRegistry.domain.repository;

import com.radiant.applicationRegistry.domain.ApplicationRegistry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRegistryRepository extends JpaRepository<ApplicationRegistry, Long> {
   Optional<ApplicationRegistry> findByAppId(String appId);
}
