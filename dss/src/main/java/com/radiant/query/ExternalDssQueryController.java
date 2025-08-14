package com.radiant.query;

import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.query.service.QueryExecutionService;
import io.swagger.annotations.Api;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/query"})
@Api(
   tags = {"Query management and execution"}
)
public class ExternalDssQueryController {
   @Autowired
   private QueryExecutionService queryExecutionService;
   @Autowired
   private DataConnectorService dataConnectorService;

   @GetMapping(
      value = {"/{queryName}"},
      produces = {"application/json"}
   )
   public Object execute(@PathVariable("queryName") String queryName, @RequestParam Map<String, String> params, HttpServletResponse servletResponse) {
      return this.queryExecutionService.execute(queryName, params, servletResponse);
   }

   @PostMapping({"/cli"})
   public String executeSql(@RequestBody @Valid CliRequest request) {
      return this.dataConnectorService.executeCliQuery(request);
   }
}
