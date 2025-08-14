package com.radiant.program.service;

import com.radiant.applicationRegistry.service.ApplicationRegistryService;
import com.radiant.dataSharingSystem.domain.DNode;
import com.radiant.dataSharingSystem.service.DataSharingSystemService;
import com.radiant.exception.dataSharingSystem.DataSharingSystemIsNotConnected;
import com.radiant.exception.program.NotSupportedProgramMethodException;
import com.radiant.exception.program.ProgramFileNotFoundException;
import com.radiant.exception.program.ProgramFileNotFoundException.ProgramNotFoundMessageCode;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.loader.EchoURLClassLoader;
import com.radiant.log.access.service.AccessLogService;
import com.radiant.log.dnodeAccess.service.DnodeAccessLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.DnodeProgramAdapter;
import com.radiant.program.QnodeProgramAdapter;
import com.radiant.program.domain.GddsExecutionStage;
import com.radiant.program.domain.GddsPostExecutionStage;
import com.radiant.program.domain.GddsPreExecutionStage;
import com.radiant.program.domain.ProgramStage;
import com.radiant.program.domain.SendProgramToDss;
import com.radiant.program.domain.dto.ExternalProgramBody;
import com.radiant.program.dto.ExecutionSite;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import com.radiant.program.dto.RdsmProgram;
import com.radiant.query.domain.dto.JudgePortalProgramResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GddsProgramExecutionServiceImpl implements GddsProgramExecutionService {
   private static final Logger LOG = LoggerFactory.getLogger(GddsProgramExecutionServiceImpl.class);
   @Autowired
   private DataSharingSystemService dataSharingSystemService;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private ResourceLoader resourceLoader;
   @Autowired
   private AccessLogService accessLogService;
   @Autowired
   private DnodeAccessLogService dnodeAccessLogService;
   @Autowired
   private ApplicationRegistryService applicationRegistryService;
   @Autowired
   private I18nService i18n;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Value("${gddslib.dir}")
   private String libraryDir;
   private final Map<String, QnodeProgramAdapter> gddsPrograms = new LinkedCaseInsensitiveMap();
   private final Map<String, RdsmProgram> gddsAnnotations = new LinkedCaseInsensitiveMap();
   private final Map<String, DnodeProgramAdapter> dssPrograms = new LinkedCaseInsensitiveMap();
   private final Map<String, RdsmProgram> dssAnnotations = new LinkedCaseInsensitiveMap();
   private static final String GDDS_PREFIX = "QNode";
   private static final String DSS_PREFIX = "DNode";

   @Autowired
   public GddsProgramExecutionServiceImpl(ListableBeanFactory beanFactory) {
      LOG.info("GddsProgramExecutionServiceImpl autowiring...");
      String[] gddsProgramNames = beanFactory.getBeanNamesForType(QnodeProgramAdapter.class);

      for(String name : gddsProgramNames) {
         RdsmProgram annotation = (RdsmProgram)beanFactory.findAnnotationOnBean(name, RdsmProgram.class);
         if (annotation != null) {
            QnodeProgramAdapter bean = (QnodeProgramAdapter)beanFactory.getBean(name, QnodeProgramAdapter.class);
            this.gddsPrograms.put(name, bean);
            this.gddsAnnotations.put(name, annotation);
            LOG.info("Registered QNode program: {}", name);
            LOG.info("Registered QNode annotation with version {} and method {}", annotation.version(), annotation.method());
         }
      }

      String[] dssProgramNames = beanFactory.getBeanNamesForType(DnodeProgramAdapter.class);

      for(String name : dssProgramNames) {
         RdsmProgram annotation = (RdsmProgram)beanFactory.findAnnotationOnBean(name, RdsmProgram.class);
         if (annotation != null) {
            DnodeProgramAdapter dssProgramBean = (DnodeProgramAdapter)beanFactory.getBean(name, DnodeProgramAdapter.class);
            this.dssPrograms.put(name, dssProgramBean);
            this.dssAnnotations.put(name, annotation);
            LOG.info("Registered DNode program: {}", name);
         }
      }

   }

   public ProgramResponse executeProgram(@Nullable Long courtId, String programName, Map<String, String> params, @Nullable ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      return this.doExecute(programName, params, programBody, (List)null, httpRequest, httpResponse);
   }

   public ProgramResponse executeProgramForDss(List<Long> dssIds, String programName, Map<String, String> params, @Nullable ExternalProgramBody programBody, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      List<DNode> dnodeCollection = (List)dssIds.stream().map((dssId) -> {
         DNode dnode = this.dataSharingSystemService.getDss(dssId);
         if (dnode.getDnodeUrl() == null) {
            throw new DataSharingSystemIsNotConnected(dnode.getId());
         } else {
            return dnode;
         }
      }).collect(Collectors.toList());
      return this.doExecute(programName, params, programBody, dnodeCollection, httpRequest, httpResponse);
   }

   public void unload(String jarToUnload, String queryName) throws IOException {
      ClassLoader cl = this.resourceLoader.getClassLoader();
      if (cl instanceof URLClassLoader) {
         ((URLClassLoader)cl).close();
         Path jar = Paths.get(jarToUnload);

           try {
              Files.delete(jar);
           } catch (NoSuchFileException e) {
              LOG.info("Program file not found for query {} while replacing", queryName);
           } catch (IOException e) {
              LOG.error("Failed to delete jar lib {}", jar.getFileName(), e);
           }

         EchoURLClassLoader loader = (EchoURLClassLoader)AccessController.doPrivileged(() -> new EchoURLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()));
         String devLibDir = System.getenv("GDDS_LIB_DIR");
         loader.init(devLibDir != null ? devLibDir : this.libraryDir);
         Thread.currentThread().setContextClassLoader(loader);
      } else {
         LOG.warn("Unexpected resource classloader, program file wasn't unloaded");
      }

   }

   public Object wrapResponse(ProgramResponse response, @Nullable Boolean unifyInterface) {
      if (response.getContentType().equals("application/json")) {
         return Boolean.TRUE.equals(unifyInterface) ? JudgePortalUtil.unifyInterfaceResponse(response, this.i18n.message("success")) : new JudgePortalProgramResponse(HttpStatus.OK, this.i18n.message("success"), (String)null, response);
      } else if (response.getContentType().equals("application/octet-stream")) {
         LOG.info("Handle file stream JP response...");
         response.getServletResponse().setHeader("Content-Disposition", "attachment;filename=\"123.txt\"");
         response.getServletResponse().setHeader("Content-Type", "application/octet-stream");
         response.getServletResponse().setHeader("Access-Control-Expose-Headers", JudgePortalUtil.EXPOSE_HEADERS);
         response.getServletResponse().setHeader("Access-Control-Allow-Headers", "*");
         response.getServletResponse().setHeader("Access-Control-Allow-Origin", "*");
         return null;
      } else {
         throw new IllegalArgumentException("Unknown program content type: " + response.getContentType());
      }
   }

   public Object wrapResponse(ProgramResponse response) {
      return this.wrapResponse(response, (Boolean)null);
   }

   private ProgramResponse doExecute(String programName, Map<String, String> params, @Nullable ExternalProgramBody requestBody, @Nullable List<DNode> forcedDNodes, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      this.applicationRegistryService.validateApiAccess(httpRequest, programName);
      boolean gddsProgramExist = this.isGddsProgramExist("QNode" + programName);
      boolean dssProgramExist = this.isDssProgramExist("DNode" + programName);
      if (!gddsProgramExist && !dssProgramExist) {
         throw new ProgramFileNotFoundException(programName, ProgramNotFoundMessageCode.PROGRAM_FILE_NOT_FOUND_ON_QNODE);
      } else {
         QnodeProgramAdapter gddsProgram = (QnodeProgramAdapter)this.gddsPrograms.get("QNode" + programName);
         RdsmProgram annotation;
         if (gddsProgramExist) {
            annotation = (RdsmProgram)this.gddsAnnotations.get("QNode" + programName);
         } else {
            annotation = (RdsmProgram)this.dssAnnotations.get("DNode" + programName);
         }

         if (annotation == null) {
            throw new RuntimeException("Failed to handle RDSM Program annotation");
         } else if (!annotation.method().toString().equals(httpRequest.getMethod())) {
            throw new NotSupportedProgramMethodException(httpRequest.getMethod());
         } else {
            ProgramRequest request = new ProgramRequest(programName);
            request.setVersion(annotation.version());
            request.setHttpVersion(httpRequest.getProtocol());
            request.getUrlParams().putAll(params);
            request.setProgramName(programName);
            request.setProgramType(annotation.type());
            request.setContentType(annotation.contentType());
            if (httpRequest.getRequestURL() != null) {
               request.setRequestedUrl(httpRequest.getRequestURL().toString());
            }

            if (forcedDNodes != null) {
               request.setCustomDnodeUrl((List)forcedDNodes.stream().map(DNode::getDnodeUrl).collect(Collectors.toList()));
            }

            if (requestBody != null) {
               request.getRequestBody().putAll(requestBody.getRest());
            }

            ProgramResponse response = new ProgramResponse();
            response.setVersion(annotation.version());
            response.setHttpVersion(httpRequest.getProtocol());
            response.setContentType(annotation.contentType());
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            if (headerNames != null) {
               while(headerNames.hasMoreElements()) {
                  String name = (String)headerNames.nextElement();
                  request.getHeaderParams().put(name, httpRequest.getHeader(name));
                  response.getHeaderParams().put(name, httpRequest.getHeader(name));
               }
            }

            response.getHeaderParams().put("Content-Type", annotation.contentType());
            if (annotation.contentType().equals("application/octet-stream")) {
               response.setServletResponse(httpResponse);
            }

            List<ProgramStage> stages = new ArrayList();
            if (gddsProgramExist) {
               ProgramStage gddsPreExec = new GddsPreExecutionStage(gddsProgram);
               this.pushStage(stages, gddsPreExec);
               if (annotation.site() == ExecutionSite.QNODE) {
                  ProgramStage gddsExec = new GddsExecutionStage(gddsProgram);
                  this.pushStage(stages, gddsExec);
               }
            }

            if (annotation.site() == ExecutionSite.DNODE) {
               ProgramStage dssProgramStage = new SendProgramToDss(this.dataSharingSystemService, this.restTemplate, this.dnodeAccessLogService, this.applicationRegistryService);
               this.pushStage(stages, dssProgramStage);
            }

            if (gddsProgramExist) {
               ProgramStage gddsPostExec = new GddsPostExecutionStage(gddsProgram);
               this.pushStage(stages, gddsPostExec);
            }

            ((ProgramStage)stages.get(0)).receiveRequest(request, response, httpRequest);
            Integer responseLength = response.getBody() != null ? response.getBody().toString().length() : null;
            this.accessLogService.logSuccess(responseLength, requestBody);
            this.serviceLogManagementService.log(LogLevel.INFO, request.toString(), HttpStatus.OK.value(), responseLength);
            return response;
         }
      }
   }

   private boolean isGddsProgramExist(String programName) {
      return this.gddsPrograms.containsKey(programName);
   }

   private boolean isDssProgramExist(String programName) {
      return this.dssPrograms.containsKey(programName);
   }

   private void pushStage(List<ProgramStage> stages, ProgramStage stage) {
      if (!stages.isEmpty()) {
         ProgramStage lastStage = (ProgramStage)stages.get(stages.size() - 1);
         lastStage.setNextStage(stage);
      }

      stages.add(stage);
   }
}
