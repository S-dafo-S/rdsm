package com.radiant.log.dnodeAccess.service;

import com.radiant.dataSharingSystem.domain.DNode;
import java.util.Date;

public interface DnodeAccessLogService {
   void log(DNode dnode, String url, Boolean success, String message);

   Long countLastLogs(DNode dnode, Date date);

   Long countLastFailedLogs(DNode dnode, Date date);

   void delete(DNode dnode);
}
