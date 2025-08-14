package com.radiant.log.dnodeAccess.service;

import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.log.dnodeAccess.domain.DnodeAccessLog;
import com.radiant.log.dnodeAccess.domain.DnodeAccessLogRepository;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DnodeAccessLogServiceImpl implements DnodeAccessLogService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(DnodeAccessLogServiceImpl.class);
   @Autowired
   private DnodeAccessLogRepository dnodeAccessLogRepository;
   @Autowired
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addShortPeriodActivity(this, "Deleting outdated DNode access logs", (Object)null);
      }

   }

   @Transactional(
      propagation = Propagation.REQUIRES_NEW
   )
   public void log(DNode dnode, String url, Boolean success, String message) {
      DnodeAccessLog event = new DnodeAccessLog();
      event.setDnode(dnode);
      event.setUrl(url);
      event.setSuccess(success);
      event.setMessage(message != null ? message : "");
      event.setTime(new Date());
      this.dnodeAccessLogRepository.saveAndFlush(event);
   }

   @Transactional(
      readOnly = true
   )
   public Long countLastLogs(DNode dnode, Date date) {
      return this.dnodeAccessLogRepository.countByDnodeAndTimeAfter(dnode, date);
   }

   @Transactional(
      readOnly = true
   )
   public Long countLastFailedLogs(DNode dnode, Date date) {
      return this.dnodeAccessLogRepository.countByDnodeAndTimeAfterAndSuccessIsFalse(dnode, date);
   }

   public void delete(DNode dnode) {
      this.dnodeAccessLogRepository.deleteByDnode(dnode);
   }

   public void performActivity(Object context) {
      LOG.trace("Deleting outdated DNode access logs");
      this.dnodeAccessLogRepository.deleteByTimeLessThan(DateUtils.addDays(new Date(), -30));
   }
}
