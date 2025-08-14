package com.radiant.program.service;

import com.radiant.exception.program.DssExecuteNotImplemented;
import com.radiant.exception.program.ProgramFileNotFoundException;
import com.radiant.exception.program.ProgramFileNotFoundException.ProgramNotFoundMessageCode;
import com.radiant.exception.query.QueryNotFoundByName;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.program.DnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import com.radiant.program.dto.RdsmProgram;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryRepository;
import com.radiant.query.service.JavaQueryArgumentResolver;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

@Service
public class DssProgramExecutionServiceImpl implements DssProgramExecutionService {
   private static final Logger LOG = LoggerFactory.getLogger(DssProgramExecutionServiceImpl.class);
   private Map<String, DnodeProgramAdapter> dssPrograms = new LinkedCaseInsensitiveMap<>();
   private Map<String, RdsmProgram> dssAnnotations = new LinkedCaseInsensitiveMap<>();
   @Autowired
   private DssQueryRepository dssQueryRepository;
   @Autowired
   private ResourceLoader resourceLoader;
   private static final String DSS_PREFIX = "DNode";

   @Autowired
   public DssProgramExecutionServiceImpl(ListableBeanFactory beanFactory) {
      String[] dssProgramNames = beanFactory.getBeanNamesForType(DnodeProgramAdapter.class);

      for(String name : dssProgramNames) {
         RdsmProgram annotation = (RdsmProgram)beanFactory.findAnnotationOnBean(name, RdsmProgram.class);
         if (annotation != null) {
            DnodeProgramAdapter dssProgramBean = (DnodeProgramAdapter)beanFactory.getBean(name, DnodeProgramAdapter.class);
            this.dssPrograms.put(name, dssProgramBean);
            this.dssAnnotations.put(name, annotation);
            LOG.info("Registered DSS program: {}", name);
         }
      }

   }

   public Object execute(String programName, ProgramRequest request, Map<String, String> urlParams, HttpServletRequest httpRequest, HttpServletResponse servletResponse) {
      if (!this.isDssProgramExist("DNode" + programName)) {
         throw new ProgramFileNotFoundException(programName, ProgramNotFoundMessageCode.PROGRAM_FILE_NOT_FOUND_ON_DNODE);
      } else {
         DssQuery query = (DssQuery)this.dssQueryRepository.findByNameIgnoreCase(programName).orElseThrow(() -> new QueryNotFoundByName(programName));
         JavaQueryArgumentResolver resolver = new JavaQueryArgumentResolver(query);
         request.setGlobalVariables(resolver.resolveGlobalVariables());
         ProgramResponse response = new ProgramResponse();
         if (request.getContentType().equals("application/octet-stream")) {
            response.setServletResponse(servletResponse);
         }

         DnodeProgramAdapter dssProgram = (DnodeProgramAdapter)this.dssPrograms.get("DNode" + programName);
         Enumeration<String> headerNames = httpRequest.getHeaderNames();
         if (headerNames != null) {
            while(headerNames.hasMoreElements()) {
               String name = (String)headerNames.nextElement();
               request.getHeaderParams().put(name, httpRequest.getHeader(name));
               response.getHeaderParams().put(name, httpRequest.getHeader(name));
            }
         }

         request.getUrlParams().clear();
         request.getUrlParams().putAll(urlParams);
         if (request.getContentType().equals("application/json")) {
            servletResponse.setHeader("Content-Type", "application/json");
         } else if (request.getContentType().equals("application/octet-stream")) {
            servletResponse.setHeader("Access-Control-Expose-Headers", JudgePortalUtil.EXPOSE_HEADERS);
            servletResponse.setHeader("Access-Control-Allow-Headers", "*");
            servletResponse.setHeader("Access-Control-Allow-Origin", "*");
            servletResponse.setHeader("Content-Type", "application/octet-stream");
         }

         try {
            dssProgram.dnodeExecute(request, response);
         } catch (NotImplementedException notImplemented) {
            throw new DssExecuteNotImplemented("Method dnodeExecute not implemented for program " + programName);
         }

         if (request.getContentType().equals("application/json")) {
            return response;
         } else if (request.getContentType().equals("application/octet-stream")) {
            return null;
         } else {
            throw new IllegalStateException("Unknown program content type {}" + request.getContentType());
         }
      }
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
      } else {
         LOG.warn("Unexpected resource classloader, program file wasn't unloaded");
      }

   }

   private boolean isDssProgramExist(String programName) {
      return this.dssPrograms.containsKey(programName);
   }
}
