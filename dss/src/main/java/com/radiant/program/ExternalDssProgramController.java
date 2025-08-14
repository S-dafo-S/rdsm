package com.radiant.program;

import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.service.DssProgramExecutionService;
import io.swagger.annotations.Api;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/program"})
@Api(
   tags = {"Program management and execution"}
)
public class ExternalDssProgramController {
   @Autowired
   private DssProgramExecutionService programExecutionService;

   @PostMapping({"/{programName}"})
   public Object execute(@PathVariable("programName") String programName, @RequestBody ProgramRequest programRequest, @RequestParam Map<String, String> params, HttpServletRequest httpRequest, HttpServletResponse response) {
      return this.programExecutionService.execute(programName, programRequest, params, httpRequest, response);
   }
}
