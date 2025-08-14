package com.radiant.fileAccess;

import com.radiant.fileAccess.service.FileAccessService;
import io.swagger.annotations.Api;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/readfile"})
@Api(
   tags = {"Getting files"}
)
public class ExternalDssFileAccessController {
   private static final Logger LOG = LoggerFactory.getLogger(ExternalDssFileAccessController.class);
   public static final String API_URL = "/api/v1/readfile";
   @Autowired
   private FileAccessService fileAccessService;

   @GetMapping(
      value = {"/{logicalPath}/**"},
      produces = {"application/octet-stream"}
   )
   public void getFile(@PathVariable("logicalPath") String logicalPath, @RequestParam(value = "ip",required = false) String ip, HttpServletRequest request, HttpServletResponse response) throws Exception {
      String internalPath = FileAccessUtils.extractRestPath(request);
      Path fileName = Paths.get(internalPath).getFileName();
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      LOG.debug("{} {}", request.getMethod(), internalPath);
      if (fileName != null) {
         response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
      }

      this.fileAccessService.read(logicalPath, internalPath, request, response);
   }

   @GetMapping({"/merge/{logicalPath}/**"})
   public List<String> mergeFiles(@PathVariable("logicalPath") String logicalPath, @RequestParam(value = "ip",required = false) String ip, @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort, @RequestParam(value = "limit",required = false,defaultValue = "0") Integer limit, HttpServletRequest request) throws Exception {
      String internalPath = FileAccessUtils.extractRestPath(request);
      request.setAttribute("CUSTOM_IP_ADDRESS", ip);
      LOG.debug("Merge files {}", internalPath);
      return this.fileAccessService.merge(logicalPath, internalPath, request, sort, limit);
   }
}
