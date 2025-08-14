package com.radiant.query.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiant.account.domain.User;
import com.radiant.applicationProperty.service.ApplicationPropertyService;
import com.radiant.auth.service.CurrentUser;
import com.radiant.dataConnector.domain.DataConnectorKind;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.query.DuplicateQueryArgumentName;
import com.radiant.exception.query.DuplicateQueryName;
import com.radiant.exception.query.FetchQueryException;
import com.radiant.exception.query.InvalidQueryArgumentNameException;
import com.radiant.exception.query.InvalidQueryNameException;
import com.radiant.exception.query.JavaQueryImplProgramNotFound;
import com.radiant.exception.query.NoSuchDssQueryImplementationException;
import com.radiant.exception.query.NoSuchQueryException;
import com.radiant.exception.query.PluginServiceNotFound;
import com.radiant.exception.query.QueryNotFoundByName;
import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.integrationFunction.domain.dto.IntegrationFunctionDto;
import com.radiant.integrationFunction.service.IntegrationFunctionService;
import com.radiant.kafka.GddsQueryEvent;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.log.service.service.ServiceLogManagementService;
import com.radiant.plugin.dto.LogLevel;
import com.radiant.program.dto.IntegrationType;
import com.radiant.program.service.DssProgramExecutionService;
import com.radiant.query.domain.DssQuery;
import com.radiant.query.domain.DssQueryImplDataConnector;
import com.radiant.query.domain.DssQueryImplementation;
import com.radiant.query.domain.DssQueryImplementationRepository;
import com.radiant.query.domain.DssQueryRepository;
import com.radiant.query.domain.JavaQueryImplementation;
import com.radiant.query.domain.JavaQueryImplementationRepository;
import com.radiant.query.domain.QueryArgument;
import com.radiant.query.domain.SqlQueryImplementation;
import com.radiant.query.domain.SqlQueryImplementationRepository;
import com.radiant.query.domain.dto.DssQueryDetailsDto;
import com.radiant.query.domain.dto.DssQueryDto;
import com.radiant.query.domain.dto.DssQueryImplDto;
import com.radiant.query.domain.dto.QueryArgumentDto;
import com.radiant.query.domain.dto.QueryDto;
import com.radiant.query.domain.dto.QueryImplConnectorDto;
import com.radiant.query.domain.dto.QueryParamDto;
import com.radiant.query.domain.dto.QueryProgramDetails;
import com.radiant.query.registry.JarEntryDto;
import com.radiant.restapi.MultipartFileResource;
import com.radiant.util.DBUtils;
import com.radiant.util.IOUtils;
import com.radiant.util.MultiPartUtils;
import com.radiant.util.ValidationUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class DssQueryServiceImpl implements DssQueryService {
   private static final Logger LOG = LoggerFactory.getLogger(DssQueryServiceImpl.class);
   @Autowired
   private DssQueryRepository dssQueryRepository;
   @Autowired
   private DssQueryImplementationRepository dssQueryImplementationRepository;
   @Autowired
   private SqlQueryImplementationRepository sqlQueryImplementationRepository;
   @Autowired
   private DataConnectorService dataConnectorService;
   @Autowired
   private JavaQueryImplementationRepository javaQueryImplementationRepository;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private ServiceLogManagementService serviceLogService;
   @Autowired
   private DssProgramExecutionService dssProgramExecutionService;
   @Autowired
   private CloseableHttpClient httpClient;
   @Value("${gddslib.dir}")
   private String libraryDir;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private ApplicationPropertyService applicationPropertyService;
   @Autowired
   private IntegrationFunctionService integrationFunctionService;

   public DssQuery getDssQuery(Long id) {
      return (DssQuery)this.dssQueryRepository.findById(id).orElseThrow(() -> new NoSuchQueryException(id));
   }

   public DssQueryDto mapQueryToDto(DssQuery query) {
      return new DssQueryDto(query, (long)query.getImplementations().size());
   }

   public List<DssQueryDto> getAllQueries() {
      return this.dssQueryRepository.findAllWithStatistic();
   }

   public DssQueryImplDto addImplementation(Long queryId, DssQueryImplDto implRequest, MultipartFile jarFile) {
      DssQuery parentQuery = this.getDssQuery(queryId);
      DssQueryImplementation result;
      switch (implRequest.getLang()) {
         case JAVA:
            if (!parentQuery.getIntegrationTypes().contains(IntegrationType.JAVA)) {
               throw new RuntimeException("Java implementation is not allowed for this query");
            }

            JavaQueryImplementation javaImpl = new JavaQueryImplementation(implRequest.getName(), parentQuery, implRequest.getIsActive());
            javaImpl.getQueryImplDataConnectors().addAll(this.constructImplDataConnectorsByRequest(javaImpl, implRequest));

            for(QueryParamDto param : implRequest.getParameters()) {
               javaImpl.getParameters().put(param.getKey(), param.getValue());
            }

            if (implRequest.getIntegrationFunction() != null && implRequest.getIntegrationFunction().getId() != null) {
               IntegrationFunction function = this.integrationFunctionService.getById(implRequest.getIntegrationFunction().getId());
               javaImpl.setIntegrationFunction(function);
            }

            if (jarFile != null) {
               try {
                  this.validateImplJarContent(parentQuery, jarFile.getBytes());
               } catch (IOException exc) {
                  LOG.error("Failed to read query impl jar", exc);
                  throw new RdsmIOException(exc);
               }

               if (implRequest.getIntegrationFunction() == null || implRequest.getIntegrationFunction().getId() == null) {
                  IntegrationFunction intFunction = this.integrationFunctionService.create(new IntegrationFunctionDto(javaImpl, jarFile.getOriginalFilename()), jarFile);
                  javaImpl.setIntegrationFunction(intFunction);
               }
            }

            result = (DssQueryImplementation)this.javaQueryImplementationRepository.save(javaImpl);
            break;
         case SQL:
            if (!parentQuery.getIntegrationTypes().contains(IntegrationType.SQL)) {
               throw new RuntimeException("SQL implementation is not allowed for this query");
            }

            this.validateSqlQueryImplDataConnectors(implRequest);
            SqlQueryImplementation sqlImpl = new SqlQueryImplementation(implRequest.getName(), parentQuery, implRequest.getIsActive());
            sqlImpl.getQueryImplDataConnectors().addAll(this.constructImplDataConnectorsByRequest(sqlImpl, implRequest));
            sqlImpl.setCode(implRequest.getCode());

            for(QueryParamDto param : implRequest.getParameters()) {
               sqlImpl.getParameters().put(param.getKey(), param.getValue());
            }

            result = (DssQueryImplementation)this.sqlQueryImplementationRepository.save(sqlImpl);
            break;
         default:
            throw new IllegalStateException("Unexpected impl language: " + implRequest.getLang());
      }

      this.auditLogService.updated((User)this.currentUser.get(), parentQuery).logMessage("Implementation is added to query {0}", new Object[]{AuditLogService.TOP_OBJECT});
      return new DssQueryImplDto(result);
   }

   public DssQueryDetailsDto getQueryDetails(Long queryId) {
      DssQuery query = this.getDssQuery(queryId);
      List<DssQueryImplementation> impls = this.dssQueryImplementationRepository.findByQueryId(queryId);
      String gddsUrl = this.applicationPropertyService.getStringValue("gdds_url");
      return new DssQueryDetailsDto(query, impls, gddsUrl);
   }

   public List<DssQueryImplDto> getImplementations(Long queryId) {
      return (List)this.dssQueryImplementationRepository.findByQueryId(queryId).stream().map(DssQueryImplDto::new).collect(Collectors.toList());
   }

   public DssQueryImplementation getImplementation(Long queryId, Long implId) {
      return (DssQueryImplementation)this.dssQueryImplementationRepository.findByIdAndQueryId(implId, queryId).orElseThrow(() -> new NoSuchDssQueryImplementationException(implId, queryId));
   }

   public DssQueryImplDto getQueryImplementation(Long queryId, Long implId) {
      return new DssQueryImplDto(this.getImplementation(queryId, implId));
   }

   public DssQueryImplDto updateImplementation(Long queryId, Long implId, DssQueryImplDto updateRequest, MultipartFile jarFile) {
      DssQueryImplementation implementation = this.getImplementation(queryId, implId);
      implementation.setName(updateRequest.getName());
      implementation.getQueryImplDataConnectors().clear();
      implementation.getQueryImplDataConnectors().addAll(this.constructImplDataConnectorsByRequest(implementation, updateRequest));
      if (implementation instanceof SqlQueryImplementation) {
         this.validateSqlQueryImplDataConnectors(updateRequest);
         SqlQueryImplementation sqlImpl = (SqlQueryImplementation)implementation;
         sqlImpl.setCode(updateRequest.getCode());
         sqlImpl.getParameters().clear();

         for(QueryParamDto param : updateRequest.getParameters()) {
            sqlImpl.getParameters().put(param.getKey(), param.getValue());
         }

         this.sqlQueryImplementationRepository.save(sqlImpl);
      } else if (implementation instanceof JavaQueryImplementation) {
         JavaQueryImplementation javaImpl = (JavaQueryImplementation)implementation;
         javaImpl.getParameters().clear();

         for(QueryParamDto param : updateRequest.getParameters()) {
            javaImpl.getParameters().put(param.getKey(), param.getValue());
         }

         try {
            if (jarFile != null) {
               this.validateImplJarContent(javaImpl.getQuery(), jarFile.getBytes());
               if (updateRequest.getIntegrationFunction().getId() != null) {
                  this.integrationFunctionService.updateFile(updateRequest.getIntegrationFunction().getId(), jarFile);
               } else {
                  IntegrationFunction intFunction = this.integrationFunctionService.create(new IntegrationFunctionDto(javaImpl, jarFile.getOriginalFilename()), jarFile);
                  javaImpl.setIntegrationFunction(intFunction);
               }
            } else if (javaImpl.getImplFilename() != null && javaImpl.getIntegrationFunction() != null) {
               String uploadedFilename = javaImpl.getIntegrationFunction().getUploadedFilename();
               Path jarPath = Paths.get(this.fullPathProgramFile(uploadedFilename));

               try {
                  this.validateImplJarContent(javaImpl.getQuery(), Files.readAllBytes(jarPath));
               } catch (NoSuchFileException var10) {
                  this.serviceLogService.error("Registered plugin file " + uploadedFilename + " not found");
                  throw new JavaQueryImplProgramNotFound(javaImpl.getImplFilename(), implementation.getQuery().getName());
               }
            }
         } catch (IOException e) {
            LOG.error("Failed to read query implementation jar file");
            throw new RdsmIOException(e);
         }

         this.javaQueryImplementationRepository.save(javaImpl);
      }

      this.auditLogService.updated((User)this.currentUser.get(), implementation.getQuery()).logMessage("Implementation of query {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new DssQueryImplDto(implementation);
   }

   public DssQueryImplDto updateImplStatus(Long queryId, Long implId, DssQueryImplDto updateRequest) {
      DssQueryImplementation impl = this.getImplementation(queryId, implId);
      impl.setIsActive(updateRequest.getIsActive());
      impl.setActivationDate(impl.getIsActive() ? new Date() : null);
      this.auditLogService.updated((User)this.currentUser.get(), impl.getQuery()).logMessage("Implementation of query {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new DssQueryImplDto((DssQueryImplementation)this.dssQueryImplementationRepository.save(impl));
   }

   public void deleteImplementation(Long queryId, Long implId) {
      DssQueryImplementation target = this.getImplementation(queryId, implId);
      DssQuery query = target.getQuery();
      this.dssQueryImplementationRepository.delete(target);
      this.auditLogService.updated((User)this.currentUser.get(), query).logMessage("Implementation of query {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});
   }

   public DssQuery getDssQuery(String name) {
      return (DssQuery)this.dssQueryRepository.findByNameIgnoreCase(name).orElseThrow(() -> new QueryNotFoundByName(name));
   }

   public boolean processQueryUpdateEvent(GddsQueryEvent gddsQueryEvent) {
      LOG.info("Received in query topic: {}", gddsQueryEvent);
      Long queryId = gddsQueryEvent.getQueryId();
      switch (gddsQueryEvent.getType()) {
         case CREATED:
            return this.handleQueryCreated(queryId);
         case UPDATED:
            return this.handleQueryUpdated(queryId);
         default:
            throw new IllegalStateException("Unexpected event type: " + gddsQueryEvent.getType());
      }
   }

   public boolean syncQueries(List<Long> queryIds) {
      boolean needRestart = false;

      for(Long gddsQueryId : queryIds) {
         QueryProgramDetails queryDetails = this.fetchQueryFromGdds(gddsQueryId);
         DssQuery query = (DssQuery)this.dssQueryRepository.findByNameIgnoreCase(queryDetails.getMetadata().getName()).orElse(new DssQuery(queryDetails.getMetadata().getName(), queryDetails.getMetadata().getCaseType()));
         this.updateQueryFromGddsResponse(query, queryDetails);
         if (queryDetails.getFile() != null) {
            needRestart = true;
         }
      }

      return needRestart;
   }

   private DssQuery updateQueryArgumentsByRequest(DssQuery target, QueryDto queryRequest) {
      this.validateQueryArguments(queryRequest.getArguments());
      target.getArguments().clear();

      for(QueryArgumentDto arg : queryRequest.getArguments()) {
         QueryArgument argument = new QueryArgument(arg.getName(), arg.getDescription());
         target.getArguments().add(argument);
      }

      return target;
   }

   private void validateQueryArguments(List<QueryArgumentDto> arguments) {
      for(QueryArgumentDto arg : arguments) {
         if (!ValidationUtils.isValidIdentifier(arg.getName())) {
            throw new InvalidQueryArgumentNameException(arg.getName());
         }
      }

   }

   private DssQuery save(DssQuery dssQuery) {
      try {
         return (DssQuery)this.dssQueryRepository.saveAndFlush(dssQuery);
      } catch (DataIntegrityViolationException exc) {
         if (DBUtils.isConstraintViolated(exc, "query_name_uniq")) {
            throw new DuplicateQueryName(dssQuery.getName());
         } else if (DBUtils.isConstraintViolated(exc, "query_argument_name_uniq")) {
            throw new DuplicateQueryArgumentName();
         } else {
            throw exc;
         }
      }
   }

   private boolean handleQueryCreated(@NotNull Long queryId) {
      QueryProgramDetails queryDetails = this.fetchQueryFromGdds(queryId);
      if (!ValidationUtils.isValidIdentifier(queryDetails.getMetadata().getName())) {
         throw new InvalidQueryNameException(queryDetails.getMetadata().getName());
      } else {
         DssQuery dssQuery = new DssQuery(queryDetails.getMetadata().getName(), queryDetails.getMetadata().getCaseType());
         this.updateQueryFromGddsResponse(dssQuery, queryDetails);
         return queryDetails.getFile() != null;
      }
   }

   private boolean handleQueryUpdated(@NotNull Long queryId) {
      QueryProgramDetails queryDetails = this.fetchQueryFromGdds(queryId);
      DssQuery dssQuery = this.getDssQuery(queryDetails.getMetadata().getName());
      this.updateQueryFromGddsResponse(dssQuery, queryDetails);
      return queryDetails.getFile() != null;
   }

   private void updateQueryFromGddsResponse(DssQuery dssQuery, QueryProgramDetails gddsResponse) {
      dssQuery.setStatus(gddsResponse.getMetadata().getStatus());
      String fileToReplace = null;
      if (gddsResponse.getFile() != null) {
         fileToReplace = dssQuery.getProgramJarName();
         dssQuery.setProgramJarName(gddsResponse.getMetadata().getProgramJarName());
      } else if (gddsResponse.getMetadata().getProgramJarName() == null) {
         fileToReplace = dssQuery.getProgramJarName();
         dssQuery.setProgramJarName((String)null);
      }

      dssQuery.setDocFileName(gddsResponse.getMetadata().getDocFileName());
      dssQuery.setSampleCodeFileName(gddsResponse.getMetadata().getSampleCodeFileName());
      this.updateQueryArgumentsByRequest(dssQuery, gddsResponse.getMetadata());
      dssQuery.getIntegrationTypes().clear();
      dssQuery.getIntegrationTypes().addAll(gddsResponse.getMetadata().getIntegrationTypes());
      dssQuery.setMethod(gddsResponse.getMetadata().getMethod());
      this.save(dssQuery);
      this.handleReceivedProgramFile(gddsResponse, fileToReplace);
   }

   private QueryProgramDetails fetchQueryFromGdds(@NonNull Long queryId) {
      String url = this.applicationPropertyService.getStringValue("gdds_url") + "/api/internal/v1/query/" + queryId + "/program";
      HttpGet get = new HttpGet(url);
      MultiValueMap<String, Object> body = null;

      try {
         CloseableHttpResponse response = this.httpClient.execute(get);
         Throwable var6 = null;

         try {
            if (response.getStatusLine().getStatusCode() != 200) {
               throw new FetchQueryException();
            }

            String boundary = MultiPartUtils.extractBoundary(response.getEntity());
            if (boundary != null) {
               int buffSize = 4096;
               MultipartStream stream = new MultipartStream(response.getEntity().getContent(), boundary.getBytes(StandardCharsets.UTF_8), 4096, (MultipartStream.ProgressNotifier)null);
               body = MultiPartUtils.parsContent(stream, QueryProgramDetails.class, this.objectMapper);
               LOG.trace("Fetched query info map size {}", body.size());
            }
         } catch (Throwable var18) {
            var6 = var18;
            throw var18;
         } finally {
            if (response != null) {
               if (var6 != null) {
                  try {
                     response.close();
                  } catch (Throwable var17) {
                     var6.addSuppressed(var17);
                  }
               } else {
                  response.close();
               }
            }

         }
      } catch (IOException e) {
         this.serviceLogService.error("Failed to parse query create/update info from QNODE");
         throw new RdsmIOException(e);
      }

      if (body != null && !body.isEmpty()) {
         QueryProgramDetails queryDetails = this.readMap(body);
         if (queryDetails.getMetadata().getProgramJarName() != null && queryDetails.getFile() == null) {
            LOG.warn("Program file {} wasn't received for query {}", queryDetails.getMetadata().getProgramJarName(), queryDetails.getMetadata().getName());
            this.serviceLogService.log(LogLevel.WARNING, "Expected program file wasn't received for query " + queryDetails.getMetadata().getName());
         }

         return queryDetails;
      } else {
         throw new FetchQueryException();
      }
   }

   private QueryProgramDetails readMap(@NonNull MultiValueMap<String, Object> input) {
      List<Object> metadata = (List)input.get("metadata");
      if (metadata != null && !metadata.isEmpty()) {
         QueryDto queryMetadata = (QueryDto)metadata.get(0);
         List<Object> files = (List)input.get("file");
         MultipartFileResource file = files == null ? null : (MultipartFileResource)files.get(0);
         return new QueryProgramDetails(queryMetadata, file);
      } else {
         throw new RuntimeException("Missing query metadata at the QNODE query create/update response");
      }
   }

   private String fullPathProgramFile(@NonNull String fileName) {
      String devLibDir = System.getenv("GDDS_LIB_DIR");
      return devLibDir != null ? devLibDir + File.separator + fileName : this.libraryDir + File.separator + fileName;
   }

   private void handleReceivedProgramFile(QueryProgramDetails query, String previousFilename) {
      try {
         if (previousFilename != null) {
            this.dssProgramExecutionService.unload(this.fullPathProgramFile(previousFilename), query.getMetadata().getName());
         }

         if (query.getFile() != null) {
            File targetJar = new File(this.fullPathProgramFile(query.getMetadata().getProgramJarName()));
            OutputStream out = new FileOutputStream(targetJar);
            FileCopyUtils.copy(query.getFile().getInputStream(), out);
            IOUtils.setReadWriteFilePermissions(targetJar);
            this.serviceLogService.log(LogLevel.INFO, "Program file updated for query " + query.getMetadata().getName());
         } else if (previousFilename != null) {
            this.serviceLogService.log(LogLevel.INFO, "Program file deleted for query " + query.getMetadata().getName());
         }

      } catch (IOException e) {
         this.serviceLogService.error("Failed to write program file for query " + query.getMetadata().getName());
         throw new RdsmIOException(e);
      }
   }

   private List<DssQueryImplDataConnector> constructImplDataConnectorsByRequest(DssQueryImplementation implementation, DssQueryImplDto updateRequest) {
      List<DssQueryImplDataConnector> result = new ArrayList();

      for(QueryImplConnectorDto namedConnector : updateRequest.getNamedConnectors()) {
         DssQueryImplDataConnector namedDc = new DssQueryImplDataConnector(implementation, namedConnector.getKey(), namedConnector.getKind());
         namedDc.setDataConnectors(this.dataConnectorService.getDataConnectors(namedConnector.getDataConnectors()));
         result.add(namedDc);
      }

      return result;
   }

   private void validateSqlQueryImplDataConnectors(DssQueryImplDto implRequest) {
      if (implRequest.getNamedConnectors().stream().anyMatch((namedConnector) -> namedConnector.getKind() == DataConnectorKind.DOCUMENT)) {
         throw new IllegalArgumentException("File connectors are not allowed for SQL implementation");
      }
   }

   private List<JarEntryDto> validateImplJarContent(DssQuery parentQuery, byte[] jarContent) {
      PluginJarContentLoader jarContentLoader = new PluginJarContentLoader(this.getClass().getClassLoader());
      jarContentLoader.addJar(jarContent);
      String expectedName = parentQuery.getName();
      List<JarEntryDto> entries = jarContentLoader.findPluginServiceClasses();
      if (entries.isEmpty()) {
         this.serviceLogService.error("Validation failed for plugin, expected service " + expectedName + " not found in the jar");
         throw new PluginServiceNotFound(expectedName);
      } else {
         boolean found = entries.stream().anyMatch((entry) -> entry.getServiceName().equalsIgnoreCase(expectedName));
         if (!found) {
            this.serviceLogService.error("Validation failed for plugin, expected service " + expectedName + " not found in the jar");
            throw new PluginServiceNotFound(expectedName);
         } else {
            return entries;
         }
      }
   }
}
