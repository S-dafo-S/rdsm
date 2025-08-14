package com.radiant.query;

import com.radiant.fileAccess.JudgePortalController;
import com.radiant.i18n.I18nService;
import com.radiant.query.domain.dto.JudgePortalQueryResponse;
import com.radiant.query.service.GddsQueryExecutionService;
import io.swagger.annotations.Api;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ParametersAreNonnullByDefault
@RequestMapping({"/api/v1/query"})
@Api(
   tags = {"Public query execution controller"}
)
public class PublicQueryController extends JudgePortalController {
   private static final Logger LOG = LoggerFactory.getLogger(PublicQueryController.class);
   @Autowired
   private I18nService i18n;
   @Autowired
   private GddsQueryExecutionService gddsQueryExecutionService;

   @GetMapping(
      value = {"/{courtId}/{queryName}"},
      produces = {"application/json"}
   )
   public JudgePortalQueryResponse execute(@PathVariable("courtId") Long courtId, @PathVariable("queryName") String queryName, @RequestParam Map<String, String> params, HttpServletRequest httpRequest) {
      String data = this.gddsQueryExecutionService.execute(courtId, queryName, params, httpRequest, false);
      return this.judgePortalResponse(data, HttpStatus.OK, "success", (String)null);
   }

   @GetMapping(
      value = {"/dss/{dssId}/{queryName}"},
      produces = {"application/json"}
   )
   public JudgePortalQueryResponse executeByDssId(@PathVariable("dssId") Long dssId, @PathVariable("queryName") String queryName, @RequestParam Map<String, String> params, HttpServletRequest httpRequest) {
      String data = this.gddsQueryExecutionService.execute(dssId, queryName, params, httpRequest, true);
      return this.judgePortalResponse(data, HttpStatus.OK, "success", (String)null);
   }

   private @NotNull JudgePortalQueryResponse judgePortalResponse(@Nullable String data, HttpStatus status, String messageCode, @Nullable String errorCode) {
      return new JudgePortalQueryResponse(status, errorCode, this.i18n.message(messageCode), data);
   }
}
