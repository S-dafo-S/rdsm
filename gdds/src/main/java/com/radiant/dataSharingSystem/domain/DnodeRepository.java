package com.radiant.dataSharingSystem.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DnodeRepository extends JpaRepository<DNode, Long> {
   Optional<DNode> findByAccountId(String accountId);

   DNode getByName(String name);

   Boolean existsByQnodeToken(String qnodeToken);

   List<DNode> findByVersionNotNull();
}
