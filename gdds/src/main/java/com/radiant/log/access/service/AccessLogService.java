package com.radiant.log.access.service;

import com.radiant.log.access.domain.dto.AccessLogDto;
import com.radiant.log.access.domain.dto.ExternalAccessLogDto;
import com.radiant.program.domain.dto.ExternalProgramBody;
import java.io.OutputStream;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccessLogService {
   void logSuccess(Integer responseLength, @Nullable ExternalProgramBody externalProgramBody);

   void logFail(Integer responseCode, String errorMessage);

   Page<AccessLogDto> getLogs(@Nullable Long start, @Nullable Long end, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration, Pageable pageable);

   void downloadLogs(OutputStream outputStream, @Nullable Long startDate, @Nullable Long endDate, @Nullable String appId, @Nullable String sysId, @Nullable String clientIp, @Nullable String username, @Nullable String userId, @Nullable String path, @Nullable Integer response, @Nullable Integer duration);

   List<ExternalAccessLogDto> getFilteredLogs(String start, @Nullable String end);
}
