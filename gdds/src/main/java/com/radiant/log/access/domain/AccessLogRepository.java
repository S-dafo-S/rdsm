package com.radiant.log.access.domain;

import java.util.Date;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long>, JpaSpecificationExecutor<AccessLog> {
   @QueryHints({@QueryHint(
   name = "org.hibernate.fetchSize",
   value = "10000"
), @QueryHint(
   name = "org.hibernate.cacheable",
   value = "false"
), @QueryHint(
   name = "org.hibernate.readOnly",
   value = "true"
)})
   @Query("SELECT a FROM AccessLog a WHERE (cast(:start as date) IS NULL OR a.startTime >= :start) AND (cast(:end as date) IS NULL OR a.startTime < :end) AND (:appId IS NULL OR a.appId = :appId) AND (:sysId IS NULL OR a.sysId = :sysId) AND (:clientIp IS NULL OR a.clientAddress = :clientIp) AND (:username IS NULL OR a.userName LIKE %:username%) AND (:userId IS NULL OR a.userId LIKE %:userId%) AND (:path IS NULL OR a.apiPath LIKE %:path%) AND (:response IS NULL OR a.responseCode = :response) AND (:duration IS NULL OR a.duration >= :duration) ORDER BY a.id")
   Stream<AccessLog> streamWithFilter(@Nullable Date start, @Nullable Date end, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Long duration);

   @Modifying
   @Transactional
   @Query("DELETE FROM AccessLog al WHERE al.endTime < :retentionDate")
   void deleteOutdatedInBulk(Date retentionDate);
}
