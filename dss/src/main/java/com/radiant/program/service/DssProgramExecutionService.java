package com.radiant.program.service;

import com.radiant.program.dto.ProgramRequest;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DssProgramExecutionService {
   Object execute(String programName, ProgramRequest request, Map<String, String> urlParams, HttpServletRequest httpRequest, HttpServletResponse servletResponse);

   void unload(String jarToUnload, String queryName) throws IOException;
}
