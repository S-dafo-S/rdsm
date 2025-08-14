package com.radiant.fileAccess;

import com.radiant.fileAccess.service.GddsFileAccessService;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.judgePortal.dto.JudgePortalResponse;
import io.swagger.annotations.Api;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping({"/api/v1/readfile"})
@Api(
   tags = {"GDDS public file access endpoint"}
)
public class PublicFileAccessController extends JudgePortalController {
   private static final Logger LOG = LoggerFactory.getLogger(PublicFileAccessController.class);
   public static final String API_URL = "/api/v1/readfile";
   @Autowired
   private GddsFileAccessService fileAccessService;
   @Autowired
   private I18nService i18n;

   @GetMapping(
      value = {"/{courtId}/{logicalPath}/**"},
      produces = {"application/octet-stream"}
   )
   public void getFile(@PathVariable("courtId") Long courtId, @PathVariable("logicalPath") String logicalPath, @RequestParam(value = "ip",required = false) String ip, HttpServletRequest request, HttpServletResponse response) {
      String internalPath = FileAccessUtils.extractRestPath(request);
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      this.fileAccessService.read(courtId, logicalPath, internalPath, response, request);
   }

   @GetMapping(
      value = {"/dnode/{logicalPath}/**"},
      produces = {"application/octet-stream"}
   )
   public void getFileByDss(@PathVariable("logicalPath") String logicalPath, @RequestParam("dnodeId") Long dnodeId, @RequestParam(value = "ip",required = false) String ip, HttpServletRequest request, HttpServletResponse response) {
      String internalPath = FileAccessUtils.extractRestPath(request);
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      this.fileAccessService.readForDss(dnodeId, logicalPath, internalPath, response, request, false);
   }

   @GetMapping({"/merge/{courtId}/{logicalPath}/**"})
   public JudgePortalResponse<List<String>> mergeFiles(@PathVariable("courtId") Long courtId, @PathVariable("logicalPath") String logicalPath, @RequestParam(value = "ip",required = false) String ip, @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort, @RequestParam(value = "limit",required = false,defaultValue = "0") Integer limit, HttpServletRequest request) {
      String internalPath = FileAccessUtils.extractRestPath(request);
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      List<String> newPath = this.fileAccessService.merge(courtId, logicalPath, internalPath, request, sort, limit);
      return JudgePortalUtil.jpResponse(newPath, HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/merge/dnode/{logicalPath}/**"})
   public JudgePortalResponse<List<String>> mergeForDss(@PathVariable("logicalPath") String logicalPath, @RequestParam("dnodeId") Long dnodeId, @RequestParam(value = "ip",required = false) String ip, @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort, @RequestParam(value = "limit",required = false,defaultValue = "0") Integer limit, HttpServletRequest request) {
      String internalPath = FileAccessUtils.extractRestPath(request);
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      List<String> newPath = this.fileAccessService.mergeForDss(dnodeId, logicalPath, internalPath, request, sort, limit);
      return JudgePortalUtil.jpResponse(newPath, HttpStatus.OK, this.i18n.message("success"), (String)null);
   }
}
