package com.radiant.log.dnodeAccess.domain;

import com.radiant.dataSharingSystem.domain.DNode;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DnodeAccessLogRepository extends JpaRepository<DnodeAccessLog, Long> {
   Long countByDnodeAndTimeAfterAndSuccessIsFalse(DNode dnode, Date date);

   Long countByDnodeAndTimeAfter(DNode dnode, Date date);

   @Modifying
   @Transactional
   @Query("DELETE FROM DnodeAccessLog dal WHERE dal.time < :retentionDate")
   void deleteByTimeLessThan(Date retentionDate);

   @Modifying
   @Transactional
   @Query("DELETE FROM DnodeAccessLog dal WHERE dal.dnode = :dnode")
   void deleteByDnode(DNode dnode);
}
