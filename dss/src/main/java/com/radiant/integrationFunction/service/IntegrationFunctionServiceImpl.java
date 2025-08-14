package com.radiant.integrationFunction.service;

import com.radiant.account.domain.User;
import com.radiant.auth.service.CurrentUser;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.integrationFunction.DuplicateIntegrationFunctionName;
import com.radiant.exception.integrationFunction.IntegrationFunctionInUse;
import com.radiant.exception.integrationFunction.IntegrationFunctionProgramNotFound;
import com.radiant.exception.integrationFunction.InvalidIntegrationFunctionConnectorKey;
import com.radiant.exception.integrationFunction.InvalidIntegrationFunctionParamKey;
import com.radiant.exception.integrationFunction.NoSuchIntegrationFunction;
import com.radiant.exception.query.DuplicatePluginClassNameException;
import com.radiant.exception.query.PluginServiceNotFound;
import com.radiant.integrationFunction.domain.IntegrationConnector;
import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.integrationFunction.domain.IntegrationFunctionRepository;
import com.radiant.integrationFunction.domain.dto.IntegrationFunctionConnectorDto;
import com.radiant.integrationFunction.domain.dto.IntegrationFunctionDto;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.query.registry.JarEntryDto;
import com.radiant.query.registry.QueryPluginEntry;
import com.radiant.query.service.PluginJarContentLoader;
import com.radiant.util.DBUtils;
import com.radiant.util.IOUtils;
import com.radiant.util.ValidationUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class IntegrationFunctionServiceImpl implements IntegrationFunctionService {
   private static final Logger LOG = LoggerFactory.getLogger(IntegrationFunctionServiceImpl.class);
   @Autowired
   private IntegrationFunctionRepository integrationFunctionRepository;
   @Autowired
   private ServiceLogManagementService serviceLogManagementService;
   @Value("${gddslib.dir}")
   private String libraryDir;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private ResourceLoader resourceLoader;

   public List<IntegrationFunctionDto> getAll() {
      return (List)this.integrationFunctionRepository.findAll().stream().map(IntegrationFunctionDto::new).collect(Collectors.toList());
   }

   public IntegrationFunction create(IntegrationFunctionDto request, MultipartFile jarFile) {
      IntegrationFunction intFunction = new IntegrationFunction(request.getName());
      this.updateByRequest(intFunction, request);
      String uploadedFilename = null;
      if (jarFile != null) {
         uploadedFilename = UUID.randomUUID() + ".jar";

         try {
            List<JarEntryDto> entries = this.validateJarContent(intFunction, jarFile.getBytes());
            this.updateRegistry(intFunction, entries);
         } catch (IOException exc) {
            LOG.error("Failed to read integration function jar", exc);
            throw new RdsmIOException(exc);
         }

         intFunction.setOriginalFilename(jarFile.getOriginalFilename());
         intFunction.setUploadedFilename(uploadedFilename);
      }

      this.save(intFunction);
      this.auditLogService.created((User)this.currentUser.get(), intFunction).logMessage("Integration function {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
      this.handleReceivedJarFile(intFunction, jarFile, (String)null, uploadedFilename);
      return intFunction;
   }

   public IntegrationFunctionDto createAndGet(IntegrationFunctionDto request, MultipartFile jarFile) {
      return new IntegrationFunctionDto(this.create(request, jarFile));
   }

   public IntegrationFunctionDto update(Long funcId, IntegrationFunctionDto request, @Nullable MultipartFile jarFile) {
      IntegrationFunction intFunction = this.getById(funcId);
      this.updateByRequest(intFunction, request);
      String fileToReplace = null;
      String uploadedFilename = null;

      try {
         List<JarEntryDto> entries = new ArrayList();
         if (jarFile != null) {
            uploadedFilename = UUID.randomUUID() + ".jar";
            entries = this.validateJarContent(intFunction, jarFile.getBytes());
            fileToReplace = intFunction.getUploadedFilename();
            intFunction.setOriginalFilename(jarFile.getOriginalFilename());
            intFunction.setUploadedFilename(uploadedFilename);
         } else if (intFunction.getUploadedFilename() != null) {
            Path jarPath = Paths.get(this.fullPathProgramFile(intFunction.getUploadedFilename()));

            try {
               entries = this.validateJarContent(intFunction, Files.readAllBytes(jarPath));
            } catch (NoSuchFileException var10) {
               this.serviceLogManagementService.error("File " + intFunction.getUploadedFilename() + " not found");
               throw new IntegrationFunctionProgramNotFound(intFunction.getOriginalFilename(), intFunction.getUploadedFilename());
            }
         }

         this.updateRegistry(intFunction, entries);
      } catch (IOException e) {
         LOG.error("Failed to read query implementation jar file");
         throw new RdsmIOException(e);
      }

      this.save(intFunction);
      this.auditLogService.updated((User)this.currentUser.get(), intFunction).logMessage("Integration function {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      this.handleReceivedJarFile(intFunction, jarFile, fileToReplace, uploadedFilename);
      return new IntegrationFunctionDto(intFunction);
   }

   public void delete(Long funcId) {
      IntegrationFunction intFunction = this.getById(funcId);
      String fileToRemove = intFunction.getUploadedFilename();
      this.auditLogService.deleted((User)this.currentUser.get(), intFunction.toAuditObject()).logMessage("Integration function {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});

      try {
         this.integrationFunctionRepository.delete(intFunction);
         this.integrationFunctionRepository.flush();
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "java_query_implementation_int_function_fk")) {
            throw new IntegrationFunctionInUse(intFunction.getName());
         }

         throw e;
      }

      this.removeJarFile(intFunction, fileToRemove);
   }

   public IntegrationFunctionDto updateFile(Long funcId, MultipartFile jarFile) {
      IntegrationFunction intFunction = this.getById(funcId);
      String fileToReplace = null;
      String uploadedFilename = null;
      if (jarFile != null) {
         uploadedFilename = UUID.randomUUID() + ".jar";
         fileToReplace = intFunction.getUploadedFilename();
         intFunction.setOriginalFilename(jarFile.getOriginalFilename());
         intFunction.setUploadedFilename(uploadedFilename);
      }

      this.save(intFunction);
      this.auditLogService.updated((User)this.currentUser.get(), intFunction).logMessage("Integration function {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      this.handleReceivedJarFile(intFunction, jarFile, fileToReplace, uploadedFilename);
      return new IntegrationFunctionDto(intFunction);
   }

   public IntegrationFunction getById(Long id) {
      return (IntegrationFunction)this.integrationFunctionRepository.findById(id).orElseThrow(() -> new NoSuchIntegrationFunction(id));
   }

   private void handleReceivedJarFile(@NonNull IntegrationFunction intFunction, MultipartFile jarFile, @Nullable String previousFilename, String newFilename) {
      try {
         this.removeJarFile(intFunction, previousFilename);
         if (newFilename != null) {
            String newFilePath = this.fullPathProgramFile(newFilename);
            File newFile = new File(newFilePath);
            OutputStream out = new FileOutputStream(newFile);
            FileCopyUtils.copy(jarFile.getInputStream(), out);
            IOUtils.setReadWriteFilePermissions(newFile);
            this.serviceLogManagementService.log(LogLevel.INFO, "File updated for integration function  " + intFunction.getName());
         } else if (previousFilename != null) {
            this.serviceLogManagementService.log(LogLevel.INFO, "File deleted for integration function " + intFunction.getName());
         }

      } catch (IOException e) {
         this.serviceLogManagementService.error("Failed to write file for integration function " + intFunction.getName());
         throw new RdsmIOException(e);
      }
   }

   void updateByRequest(IntegrationFunction target, IntegrationFunctionDto request) {
      for(IntegrationFunctionConnectorDto connector : request.getConnectors()) {
         if (!ValidationUtils.isValidIdentifier(connector.getKey())) {
            throw new InvalidIntegrationFunctionConnectorKey(connector.getKey());
         }
      }

      for(String param : request.getParams()) {
         if (!ValidationUtils.isValidIdentifier(param)) {
            throw new InvalidIntegrationFunctionParamKey(param);
         }
      }

      target.setDescription(request.getDescription());
      target.getConnectors().clear();

      for(IntegrationFunctionConnectorDto connRequest : request.getConnectors()) {
         target.getConnectors().add(new IntegrationConnector(connRequest.getKey(), connRequest.getKind()));
      }

      target.getParameters().clear();

      for(String param : request.getParams()) {
         target.getParameters().add(param);
      }

   }

   private IntegrationFunction save(IntegrationFunction integrationFunction) {
      try {
         return (IntegrationFunction)this.integrationFunctionRepository.saveAndFlush(integrationFunction);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "integration_function_name_uniq")) {
            throw new DuplicateIntegrationFunctionName(integrationFunction.getName());
         } else {
            throw exc;
         }
      }
   }

   private void removeJarFile(@NonNull IntegrationFunction intFunction, @Nullable String filename) {
      ClassLoader cl = this.resourceLoader.getClassLoader();
      if (cl instanceof URLClassLoader) {
         try {
            if (filename != null) {
               ((URLClassLoader)cl).close();
               Path previousFilePath = Paths.get(this.fullPathProgramFile(filename));

               try {
                  Files.delete(previousFilePath);
               } catch (NoSuchFileException var6) {
                  LOG.warn("File not found for integration function {} while replacing", intFunction.getName());
               }
            }
         } catch (IOException e) {
            this.serviceLogManagementService.error("Failed to remove file for integration function " + intFunction.getName());
            throw new RdsmIOException(e);
         }
      } else {
         LOG.warn("Unexpected resource classloader, integration file file wasn't removed");
      }

   }

   private String fullPathProgramFile(@NonNull String fileName) {
      String devLibDir = System.getenv("GDDS_LIB_DIR");
      return devLibDir != null ? devLibDir + File.separator + fileName : this.libraryDir + File.separator + fileName;
   }

   private List<JarEntryDto> validateJarContent(IntegrationFunction integrationFunction, byte[] jarContent) {
      PluginJarContentLoader jarContentLoader = new PluginJarContentLoader(this.getClass().getClassLoader());
      jarContentLoader.addJar(jarContent);
      List<JarEntryDto> entries = jarContentLoader.findPluginServiceClasses();
      if (entries.isEmpty()) {
         this.serviceLogManagementService.error("Validation failed for plugin, expected service not found in the jar");
         throw new PluginServiceNotFound("ANY");
      } else {
         for(JarEntryDto entry : entries) {
            List<IntegrationFunction> duplicates = this.integrationFunctionRepository.getDuplicatesByPluginClassName(integrationFunction.getId(), entry.getClassName());
            if (!duplicates.isEmpty()) {
               IntegrationFunction duplicateFunc = (IntegrationFunction)duplicates.get(0);
               this.serviceLogManagementService.error("Validation failed for plugin, class " + entry.getClassName() + " already registered for integration function " + duplicateFunc.getName());
               throw new DuplicatePluginClassNameException(entry.getClassName(), duplicateFunc.getName());
            }
         }

         return entries;
      }
   }

   private void updateRegistry(IntegrationFunction targetFunc, List<JarEntryDto> entryDtoList) {
      targetFunc.getPluginEntries().clear();

      for(JarEntryDto entryDto : entryDtoList) {
         QueryPluginEntry entry = new QueryPluginEntry();
         entry.setServiceName(entryDto.getServiceName());
         entry.setClassName(entryDto.getClassName());
         targetFunc.getPluginEntries().add(entry);
      }

   }
}
