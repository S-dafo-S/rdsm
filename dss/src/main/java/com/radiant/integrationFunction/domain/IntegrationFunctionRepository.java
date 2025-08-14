package com.radiant.integrationFunction.domain;

import java.util.List;
import javax.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationFunctionRepository extends JpaRepository<IntegrationFunction, Long> {
   @Query("SELECT func FROM com.radiant.integrationFunction.domain.IntegrationFunction func JOIN func.pluginEntries e WHERE (:funcId IS NULL OR func.id <> :funcId) AND e.className = :className")
   List<IntegrationFunction> getDuplicatesByPluginClassName(@Nullable Long funcId, String className);
}
