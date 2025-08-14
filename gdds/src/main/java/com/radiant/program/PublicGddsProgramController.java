package com.radiant.program;

import com.radiant.fileAccess.JudgePortalController;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.program.dto.ProgramResponse;
import com.radiant.program.service.GddsProgramExecutionService;
import io.swagger.annotations.Api;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@Api(
   tags = {"Program operations"}
)
public class PublicGddsProgramController extends JudgePortalController {
   private static final Logger LOG = LoggerFactory.getLogger(PublicGddsProgramController.class);
   @Autowired
   private GddsProgramExecutionService programExecutionService;

   @GetMapping({"/api/v1/program/{courtId}/{programName}"})
   public Object execute(@PathVariable("courtId") Long courtId, @PathVariable("programName") String programName, @RequestParam Map<String, String> params, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgram(courtId, programName, params, (ExternalProgramBody)null, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }

   @PostMapping({"/api/v1/program/{courtId}/{programName}"})
   public Object executePostProgram(@PathVariable("courtId") Long courtId, @PathVariable("programName") String programName, @RequestParam Map<String, String> params, @RequestBody(required = false) ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgram(courtId, programName, params, programBody, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }

   @GetMapping({"/radiant/rdsm/api/v1/program/{programName}"})
   public Object execute(@PathVariable("programName") String programName, @RequestParam(value = "court",required = false) Long courtId, @RequestParam Map<String, String> params, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgram(courtId, programName, params, (ExternalProgramBody)null, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }

   @PostMapping({"/radiant/rdsm/api/v1/program/{programName}"})
   public Object executePostProgram(@PathVariable("programName") String programName, @RequestParam(value = "court",required = false) Long courtId, @RequestParam Map<String, String> params, @RequestBody(required = false) ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgram(courtId, programName, params, programBody, httpRequest, httpResponse);
      Boolean unifyInterface = true;
      return this.programExecutionService.wrapResponse(data, unifyInterface);
   }

   @GetMapping({"/api/v1/program/dnode/{dssId}/{programName}"})
   public Object executeForDss(@PathVariable("dssId") Long dssId, @PathVariable("programName") String programName, @RequestParam Map<String, String> params, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgramForDss(Collections.singletonList(dssId), programName, params, (ExternalProgramBody)null, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }

   @GetMapping({"/api/v1/program/dnode/{programName}"})
   public Object executeForDssList(@PathVariable("programName") String programName, @RequestParam("dnodeIds") List<Long> dnodeIds, @RequestParam Map<String, String> params, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      LOG.info("executeForDssList. Dss Ids:");
      LOG.info((String)dnodeIds.stream().map(Object::toString).collect(Collectors.joining(", ")));
      ProgramResponse data = this.programExecutionService.executeProgramForDss(dnodeIds, programName, params, (ExternalProgramBody)null, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }

   @PostMapping({"/api/v1/program/dnode/{programName}"})
   public Object executePostProgram(@PathVariable("programName") String programName, @RequestParam("dnodeIds") List<Long> dnodeIds, @RequestParam Map<String, String> params, @RequestBody(required = false) ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      ProgramResponse data = this.programExecutionService.executeProgramForDss(dnodeIds, programName, params, programBody, httpRequest, httpResponse);
      return this.programExecutionService.wrapResponse(data);
   }
}
