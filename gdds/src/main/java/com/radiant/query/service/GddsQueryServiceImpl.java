package com.radiant.query.service;

import com.radiant.account.domain.User;
import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.auth.service.CurrentUser;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.program.DuplicateProgramClassNameException;
import com.radiant.exception.program.DuplicateProgramServiceNameException;
import com.radiant.exception.program.InvalidIntegrationType;
import com.radiant.exception.program.ProgramFileNotFoundException;
import com.radiant.exception.program.ProgramServiceNotFound;
import com.radiant.exception.program.ProgramFileNotFoundException.ProgramNotFoundMessageCode;
import com.radiant.exception.query.DuplicateQueryArgumentName;
import com.radiant.exception.query.DuplicateQueryName;
import com.radiant.exception.query.InvalidQueryArgumentNameException;
import com.radiant.exception.query.InvalidQueryNameException;
import com.radiant.exception.query.NoSuchQueryException;
import com.radiant.exception.query.QueryNotFoundByName;
import com.radiant.kafka.GddsQueryEvent;
import com.radiant.kafka.GddsQueryEventType;
import com.radiant.kafka.service.KafkaService;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.domain.dto.ProgramJarEntryDto;
import com.radiant.program.dto.IntegrationType;
import com.radiant.program.dto.RdsmProgram;
import com.radiant.program.registry.ProgramEntry;
import com.radiant.program.registry.ProgramJarLoader;
import com.radiant.program.service.GddsProgramExecutionService;
import com.radiant.query.domain.GddsQuery;
import com.radiant.query.domain.GddsQueryRepository;
import com.radiant.query.domain.QueryArgument;
import com.radiant.query.domain.SyncStatus;
import com.radiant.query.domain.dto.GddsQueryDetailsDto;
import com.radiant.query.domain.dto.GddsQueryDto;
import com.radiant.query.domain.dto.QueryArgumentDto;
import com.radiant.query.registry.JarEntryDto;
import com.radiant.restapi.RestApiUtils;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import com.radiant.util.DBUtils;
import com.radiant.util.IOUtils;
import com.radiant.util.ValidationUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class GddsQueryServiceImpl implements GddsQueryService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(GddsQueryServiceImpl.class);
   @Autowired
   private GddsQueryRepository gddsQueryRepository;
   @Nullable
   @Autowired(
      required = false
   )
   private KafkaService kafkaService;
   @Value("${kafka.dss.topic.query}")
   private String dssQueryTopic;
   @Autowired
   private GddsProgramExecutionService gddsProgramExecutionService;
   @Autowired
   private ServiceLogManagementService serviceLogService;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;
   @Value("${gddslib.dir}")
   private String libraryDir;
   @Value("${gdds.fs.programdir}")
   private String programFsDir;

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addMinutelyActivity(this, "Send query sync messages", (Object)null);
      }

   }

   public void performActivity(Object context) {
      List<GddsQuery> created = this.gddsQueryRepository.getBySyncStatus(SyncStatus.CREATED_NOT_SYNCED);
      List<GddsQuery> updated = this.gddsQueryRepository.getBySyncStatus(SyncStatus.UPDATED_NOT_SYNCED);

      for(GddsQuery query : created) {
         query.setSyncStatus(SyncStatus.SYNCED);
      }

      for(GddsQuery query : updated) {
         query.setSyncStatus(SyncStatus.SYNCED);
      }

      this.gddsQueryRepository.saveAll(created);
      this.gddsQueryRepository.saveAll(updated);

      for(GddsQuery query : created) {
         this.sendKafkaMessage(GddsQueryEventType.CREATED, query.getId());
      }

      for(GddsQuery query : updated) {
         this.sendKafkaMessage(GddsQueryEventType.UPDATED, query.getId());
      }

   }

   public List<GddsQueryDto> getAllQueries() {
      return (List)this.gddsQueryRepository.findAll().stream().map(GddsQueryDto::new).collect(Collectors.toList());
   }

   public GddsQueryDetailsDto getQueryDetails(Long queryId) {
      return new GddsQueryDetailsDto(this.getQuery(queryId));
   }

   public MultiValueMap<String, Object> getQueryProgramDetails(@NotNull Long queryId) {
      String queryMetadata = "metadata";
      String queryProgramFile = "file";
      MultiValueMap<String, Object> result = new LinkedMultiValueMap();
      GddsQueryDetailsDto queryDetails = this.getQueryDetails(queryId);
      result.set("metadata", queryDetails);
      if (queryDetails.getProgramJarName() != null) {
         LOG.trace("Reading program file for query {}", queryDetails.getName());
         File jar = new File(this.fullPathProgramFile(queryDetails.getProgramJarName()));

         try {
            FileInputStream in = new FileInputStream(jar);
            InputStreamResource is = new InputStreamResource(in);
            result.set("file", is);
         } catch (FileNotFoundException var9) {
            LOG.warn("Program jar {} not found for query {}", queryDetails.getProgramJarName(), queryDetails.getName());
         }
      }

      return result;
   }

   public GddsQueryDetailsDto updateQuery(Long queryId, GddsQueryDetailsDto updateRequest, MultipartFile jarFile, MultipartFile docFile, MultipartFile sampleCodeFile) {
      GddsQuery target = this.getQuery(queryId);
      target.setStatus(updateRequest.getStatus());
      this.updateQueryArgumentsByRequest(target, updateRequest);
      String fileToReplace = null;
      String docFileToReplace = null;
      String newDocFileName = null;
      String sampleCodeFileToReplace = null;
      String newSampleCodeFileName = null;

      try {
         ProgramJarEntryDto programJarEntry = new ProgramJarEntryDto();
         if (jarFile != null) {
            fileToReplace = target.getProgramJarName();
            target.setProgramJarName(jarFile.getOriginalFilename());
            programJarEntry = this.validateProgramJarContent(target, jarFile.getBytes());
         } else if (updateRequest.getProgramJarName() == null) {
            fileToReplace = target.getProgramJarName();
            target.setProgramJarName((String)null);
         } else {
            Path jarPath = Paths.get(this.fullPathProgramFile(target.getProgramJarName()));

            try {
               programJarEntry = this.validateProgramJarContent(target, Files.readAllBytes(jarPath));
            } catch (NoSuchFileException var15) {
               this.serviceLogService.error("Registered program file " + updateRequest.getProgramJarName() + " not found");
               throw new ProgramFileNotFoundException(target.getProgramJarName(), ProgramNotFoundMessageCode.PROGRAM_FILE_NOT_FOUND_ON_QNODE);
            }
         }

         this.updateIntegrationTypes(target, programJarEntry.getIntegrationTypes());
         target.setMethod(programJarEntry.getMethod());
         this.updateRegistry(target, programJarEntry.getEntries());
         if (docFile != null) {
            newDocFileName = UUID.randomUUID().toString();
            docFileToReplace = target.getUploadedDocFileName();
            target.setUploadedDocFileName(newDocFileName);
            target.setDocFileName(docFile.getOriginalFilename());
         }

         if (sampleCodeFile != null) {
            newSampleCodeFileName = UUID.randomUUID().toString();
            sampleCodeFileToReplace = target.getUploadedSampleCodeFileName();
            target.setUploadedSampleCodeFileName(newSampleCodeFileName);
            target.setSampleCodeFileName(sampleCodeFile.getOriginalFilename());
         }
      } catch (IOException exc) {
         LOG.error("Failed to read program jar file");
         throw new RdsmIOException(exc);
      }

      boolean delaySendMessage = jarFile != null;
      target.setSyncStatus(delaySendMessage ? SyncStatus.UPDATED_NOT_SYNCED : SyncStatus.SYNCED);
      GddsQueryDetailsDto result = new GddsQueryDetailsDto(this.save(target));
      this.auditLogService.updated((User)this.currentUser.get(), target).logMessage("Query {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      this.handleReceivedDocFile(target, docFile, docFileToReplace, newDocFileName);
      this.handleReceivedSampleCodeFile(target, sampleCodeFile, sampleCodeFileToReplace, newSampleCodeFileName);
      this.handleReceivedProgramFile(target, jarFile, fileToReplace);
      if (!delaySendMessage) {
         this.sendKafkaMessage(GddsQueryEventType.UPDATED, result.getId());
      }

      return result;
   }

   public GddsQueryDetailsDto patchQuery(Long queryId, GddsQueryDetailsDto updateRequest) {
      GddsQuery target = this.getQuery(queryId);
      target.setSyncStatus(SyncStatus.SYNCED);
      UpdateEntityHelper.ifNotNull(updateRequest.getStatus(), target::setStatus);
      GddsQueryDetailsDto result = new GddsQueryDetailsDto(this.save(target));
      this.auditLogService.updated((User)this.currentUser.get(), target).logMessage("Query {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      this.sendKafkaMessage(GddsQueryEventType.UPDATED, result.getId());
      return result;
   }

   public GddsQueryDetailsDto createQuery(GddsQueryDetailsDto createRequest, MultipartFile jarFile, MultipartFile docFile, MultipartFile sampleCodeFile) {
      if (!ValidationUtils.isValidIdentifier(createRequest.getName())) {
         throw new InvalidQueryNameException(createRequest.getName());
      } else {
         GddsQuery query = new GddsQuery(createRequest.getName(), createRequest.getStatus(), createRequest.getCaseType());
         this.updateQueryArgumentsByRequest(query, createRequest);
         if (jarFile != null) {
            try {
               query.setProgramJarName(jarFile.getOriginalFilename());
               ProgramJarEntryDto programJarEntry = this.validateProgramJarContent(query, jarFile.getBytes());
               this.updateIntegrationTypes(query, programJarEntry.getIntegrationTypes());
               query.setMethod(programJarEntry.getMethod());
               this.updateRegistry(query, programJarEntry.getEntries());
            } catch (IOException exc) {
               LOG.error("Failed to read program jar", exc);
               throw new RdsmIOException(exc);
            }
         }

         String newDocFileName = null;
         String newSampleCodeFileName = null;
         if (docFile != null) {
            newDocFileName = UUID.randomUUID().toString();
            query.setUploadedDocFileName(newDocFileName);
            query.setDocFileName(docFile.getOriginalFilename());
         }

         if (sampleCodeFile != null) {
            newSampleCodeFileName = UUID.randomUUID().toString();
            query.setUploadedSampleCodeFileName(newSampleCodeFileName);
            query.setSampleCodeFileName(sampleCodeFile.getOriginalFilename());
         }

         boolean delaySendMessage = jarFile != null;
         if (!delaySendMessage) {
            query.setSyncStatus(SyncStatus.SYNCED);
         }

         GddsQueryDetailsDto result = new GddsQueryDetailsDto(this.save(query));
         this.auditLogService.updated((User)this.currentUser.get(), query).logMessage("Query {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
         this.handleReceivedDocFile(query, docFile, (String)null, newDocFileName);
         this.handleReceivedSampleCodeFile(query, sampleCodeFile, (String)null, newSampleCodeFileName);
         this.handleReceivedProgramFile(query, jarFile, (String)null);
         if (!delaySendMessage) {
            this.sendKafkaMessage(GddsQueryEventType.CREATED, query.getId());
         }

         return result;
      }
   }

   public ResponseEntity<InputStreamResource> downloadDoc(String queryName) {
      GddsQuery query = this.getQueryByName(queryName);
      String fileName = query.getDocFileName();
      if (fileName != null && query.getUploadedDocFileName() != null) {
         int lastIndexOfPoint = fileName.lastIndexOf(46);
         MediaType mediaType = MediaType.TEXT_PLAIN;
         if (lastIndexOfPoint >= 0) {
            String extension = fileName.substring(lastIndexOfPoint + 1).toLowerCase();
            switch (extension) {
               case "doc":
                  mediaType = MediaType.parseMediaType("application/msword");
                  break;
               case "pdf":
                  mediaType = MediaType.APPLICATION_PDF;
                  break;
               default:
                  throw new IllegalStateException("Unexpected extension: " + extension);
            }
         }

         HttpHeaders headers = RestApiUtils.getHttpResponseAttachmentHeaders(fileName);
         ResponseEntity.BodyBuilder response = ((ResponseEntity.BodyBuilder)ResponseEntity.ok().headers(headers)).contentType(mediaType);
         File file = new File(this.fullDocFilePath(query.getUploadedDocFileName(), query.getId()));

         try {
            return response.body(new InputStreamResource(new FileInputStream(file)));
         } catch (FileNotFoundException var10) {
            throw new RuntimeException("Doc file not found for query " + query.getName());
         }
      } else {
         return null;
      }
   }

   public ResponseEntity<InputStreamResource> downloadSampleCode(String queryName) {
      GddsQuery query = this.getQueryByName(queryName);
      String fileName = query.getSampleCodeFileName();
      if (fileName != null && query.getUploadedSampleCodeFileName() != null) {
         int lastIndexOfPoint = fileName.lastIndexOf(46);
         MediaType mediaType = MediaType.TEXT_PLAIN;
         if (lastIndexOfPoint >= 0) {
            String extension = fileName.substring(lastIndexOfPoint + 1).toLowerCase();
            switch (extension) {
               case "jar":
                  mediaType = MediaType.parseMediaType("application/java-archive");
                  break;
               default:
                  throw new IllegalStateException("Unexpected extension: " + extension);
            }
         }

         HttpHeaders headers = RestApiUtils.getHttpResponseAttachmentHeaders(fileName);
         ResponseEntity.BodyBuilder response = ((ResponseEntity.BodyBuilder)ResponseEntity.ok().headers(headers)).contentType(mediaType);
         File file = new File(this.fullSampleCodeFilePath(query.getUploadedSampleCodeFileName(), query.getId()));

         try {
            return response.body(new InputStreamResource(new FileInputStream(file)));
         } catch (FileNotFoundException var10) {
            throw new RuntimeException("Sample code file not found for query " + query.getName());
         }
      } else {
         return null;
      }
   }

   private GddsQuery getQuery(Long queryId) {
      return (GddsQuery)this.gddsQueryRepository.findById(queryId).orElseThrow(() -> new NoSuchQueryException(queryId));
   }

   private GddsQuery getQueryByName(String queryName) {
      return (GddsQuery)this.gddsQueryRepository.findByNameIgnoreCase(queryName).orElseThrow(() -> new QueryNotFoundByName(queryName));
   }

   private void updateQueryArgumentsByRequest(GddsQuery query, GddsQueryDetailsDto request) {
      this.validateQueryArguments(request.getArguments());
      query.getArguments().clear();

      for(QueryArgumentDto arg : request.getArguments()) {
         query.getArguments().add(new QueryArgument(arg.getName(), arg.getDescription()));
      }

   }

   private void validateQueryArguments(List<QueryArgumentDto> arguments) {
      for(QueryArgumentDto arg : arguments) {
         if (!ValidationUtils.isValidIdentifier(arg.getName())) {
            throw new InvalidQueryArgumentNameException(arg.getName());
         }
      }

   }

   private GddsQuery save(@NotNull GddsQuery query) {
      try {
         return (GddsQuery)this.gddsQueryRepository.saveAndFlush(query);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "query_name_uniq")) {
            throw new DuplicateQueryName(query.getName());
         } else if (DBUtils.isConstraintViolated(exc, "query_argument_name_uniq")) {
            throw new DuplicateQueryArgumentName();
         } else {
            throw exc;
         }
      }
   }

   private void sendKafkaMessage(GddsQueryEventType type, @Nullable Long queryId) {
      if (this.kafkaService != null) {
         this.kafkaService.sendMessage(this.dssQueryTopic, new GddsQueryEvent(type, queryId));
      } else {
         LOG.warn("Kafka isn't active");
      }

   }

   private String fullPathProgramFile(@NonNull String fileName) {
      String devLibDir = System.getenv("GDDS_LIB_DIR");
      return devLibDir != null ? devLibDir + File.separator + fileName : this.libraryDir + File.separator + fileName;
   }

   private void handleReceivedProgramFile(GddsQuery query, MultipartFile jarFile, String previousFilename) {
      String filename = jarFile != null ? jarFile.getOriginalFilename() : null;

      try {
         if (previousFilename != null) {
            this.gddsProgramExecutionService.unload(this.fullPathProgramFile(previousFilename), query.getName());
         }

         if (filename != null) {
            File targetJar = new File(this.fullPathProgramFile(filename));
            OutputStream out = new FileOutputStream(targetJar);
            FileCopyUtils.copy(jarFile.getInputStream(), out);
            IOUtils.setReadWriteFilePermissions(targetJar);
            this.serviceLogService.log(LogLevel.INFO, "Program file updated for query " + query.getName());
         } else if (previousFilename != null) {
            this.serviceLogService.log(LogLevel.INFO, "Program file deleted for query " + query.getName());
         }

      } catch (IOException e) {
         this.serviceLogService.error("Failed to write program file for query " + query.getName());
         throw new RdsmIOException(e);
      }
   }

   private void handleReceivedDocFile(GddsQuery query, MultipartFile docFile, String previousFilename, String newFilename) {
      try {
         if (previousFilename != null) {
            Path docPath = Paths.get(this.fullDocFilePath(previousFilename, query.getId()));

            try {
               Files.delete(docPath);
            } catch (NoSuchFileException var9) {
               LOG.warn("Doc file not found for query {} while replacing", query.getName());
            }
         }

         if (newFilename != null) {
            String docRootPath = this.getDocRootPath(query.getId());
            File docRoot = new File(docRootPath);
            if (!docRoot.exists()) {
               Files.createDirectories(docRoot.toPath());
            }

            File targetDoc = new File(this.fullDocFilePath(newFilename, query.getId()));
            OutputStream out = new FileOutputStream(targetDoc);
            FileCopyUtils.copy(docFile.getInputStream(), out);
            IOUtils.setReadWriteFilePermissions(targetDoc);
            this.serviceLogService.log(LogLevel.INFO, "Doc file updated for query " + query.getName());
         } else if (previousFilename != null) {
            this.serviceLogService.log(LogLevel.INFO, "Doc file deleted for query " + query.getName());
         }

      } catch (IOException e) {
         this.serviceLogService.error("Failed to write doc file for query " + query.getName());
         throw new RdsmIOException(e);
      }
   }

   private void handleReceivedSampleCodeFile(GddsQuery query, MultipartFile sampleCodeFile, String previousFilename, String newFilename) {
      try {
         if (previousFilename != null) {
            Path sampleCodePath = Paths.get(this.fullSampleCodeFilePath(previousFilename, query.getId()));

            try {
               Files.delete(sampleCodePath);
            } catch (NoSuchFileException var9) {
               LOG.warn("Sample code file not found for query {} while replacing", query.getName());
            }
         }

         if (newFilename != null) {
            String sampleCodeRootPath = this.getSampleCodeRootPath(query.getId());
            File sampleCodeRoot = new File(sampleCodeRootPath);
            if (!sampleCodeRoot.exists()) {
               Files.createDirectories(sampleCodeRoot.toPath());
            }

            File targetSampleCode = new File(this.fullSampleCodeFilePath(newFilename, query.getId()));
            OutputStream out = new FileOutputStream(targetSampleCode);
            FileCopyUtils.copy(sampleCodeFile.getInputStream(), out);
            IOUtils.setReadWriteFilePermissions(targetSampleCode);
            this.serviceLogService.log(LogLevel.INFO, "Sample code file updated for query " + query.getName());
         } else if (previousFilename != null) {
            this.serviceLogService.log(LogLevel.INFO, "Sample code file deleted for query " + query.getName());
         }

      } catch (IOException e) {
         this.serviceLogService.error("Failed to write sample code file for query " + query.getName());
         throw new RdsmIOException(e);
      }
   }

   private ProgramJarEntryDto validateProgramJarContent(GddsQuery query, byte[] jarContent) {
      ProgramJarLoader jarContentLoader = new ProgramJarLoader(this.getClass().getClassLoader());
      jarContentLoader.addJar(jarContent);
      String gddsPrefix = "QNode";
      String dssPrefix = "DNode";
      String expectedGddsName = "QNode" + query.getName();
      String expectedDssName = "DNode" + query.getName();
      List<JarEntryDto> entries = jarContentLoader.findProgramServiceClasses();
      if (entries.isEmpty()) {
         this.serviceLogService.error("Validation failed for program, expected service " + expectedGddsName + " or " + expectedDssName + " not found in the jar");
         throw new ProgramServiceNotFound(expectedGddsName, expectedDssName);
      } else {
         boolean found = entries.stream().anyMatch((entryx) -> entryx.getServiceName().equalsIgnoreCase(expectedGddsName) || entryx.getServiceName().equalsIgnoreCase(expectedDssName));
         if (!found) {
            this.serviceLogService.error("Validation failed for program, expected service " + expectedGddsName + " or " + expectedDssName + " not found in the jar");
            throw new ProgramServiceNotFound(expectedGddsName, expectedDssName);
         } else {
            for(JarEntryDto entry : entries) {
               List<GddsQuery> duplicatesByClassName = this.gddsQueryRepository.getDuplicatesByClassName(query.getId(), entry.getClassName());
               if (!duplicatesByClassName.isEmpty()) {
                  this.serviceLogService.error("Validation failed for program, class " + entry.getClassName() + " already registered for query " + query.getName());
                  throw new DuplicateProgramClassNameException(entry.getClassName(), ((GddsQuery)duplicatesByClassName.get(0)).getName());
               }

               List<GddsQuery> duplicatesByServiceName = this.gddsQueryRepository.getDuplicatesByServiceName(query.getId(), entry.getServiceName());
               if (!duplicatesByServiceName.isEmpty()) {
                  this.serviceLogService.error("Validation failed for program, service " + entry.getServiceName() + " already registered for query " + query.getName());
                  throw new DuplicateProgramServiceNameException(entry.getServiceName(), ((GddsQuery)duplicatesByClassName.get(0)).getName());
               }
            }

            RdsmProgram rdsmProgram = jarContentLoader.findProgramAnnotation();
            IntegrationType[] typesArray = rdsmProgram.integrationTypes();
            List<IntegrationType> types = new ArrayList(Arrays.asList(typesArray));
            LOG.trace("Found RdsmProgram annotation with integrationType {}", types.stream().map(Enum::name).collect(Collectors.joining(", ")));
            return new ProgramJarEntryDto(types, rdsmProgram.method(), entries);
         }
      }
   }

   private void updateRegistry(GddsQuery targetQuery, List<JarEntryDto> entryDtoList) {
      targetQuery.getProgramEntries().clear();

      for(JarEntryDto entryDto : entryDtoList) {
         ProgramEntry entry = new ProgramEntry();
         entry.setServiceName(entryDto.getServiceName());
         entry.setClassName(entryDto.getClassName());
         targetQuery.getProgramEntries().add(entry);
      }

   }

   private void updateIntegrationTypes(GddsQuery targetQuery, Collection<IntegrationType> typesToSet) {
      if (targetQuery.getIntegrationTypes().retainAll(typesToSet)) {
         throw new InvalidIntegrationType(targetQuery.getIntegrationTypes(), typesToSet);
      } else {
         targetQuery.getIntegrationTypes().clear();
         targetQuery.getIntegrationTypes().addAll(typesToSet);
      }
   }

   private String getDocRootPath(@NotNull Long queryId) {
      return this.programFsDir + File.separator + queryId + File.separator + "doc";
   }

   private String fullDocFilePath(@NonNull String fileName, @NotNull Long queryId) {
      return this.getDocRootPath(queryId) + File.separator + fileName;
   }

   private String getSampleCodeRootPath(@NotNull Long queryId) {
      return this.programFsDir + File.separator + queryId + File.separator + "sampleCode";
   }

   private String fullSampleCodeFilePath(@NonNull String fileName, @NotNull Long queryId) {
      return this.getSampleCodeRootPath(queryId) + File.separator + fileName;
   }
}
