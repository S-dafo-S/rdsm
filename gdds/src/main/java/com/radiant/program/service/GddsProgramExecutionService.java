package com.radiant.program.service;

import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.program.dto.ProgramResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GddsProgramExecutionService {
   /** @deprecated */
   @Deprecated
   ProgramResponse executeProgram(@Nullable Long courtId, String programName, Map<String, String> params, @Nullable ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

   ProgramResponse executeProgramForDss(List<Long> dssId, String programName, Map<String, String> params, @Nullable ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

   void unload(String jarToUnload, String queryName) throws IOException;

   Object wrapResponse(ProgramResponse response, @Nullable Boolean unifyInterface);

   Object wrapResponse(ProgramResponse response);
}
