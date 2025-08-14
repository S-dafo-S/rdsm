package com.radiant.dataConnector.service;

import com.google.common.collect.ImmutableList;
import com.radiant.account.domain.User;
import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.auth.service.CurrentUser;
import com.radiant.court.domain.DssHostedCourt;
import com.radiant.court.service.DssCourtService;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.DataConnectorType;
import com.radiant.dataConnector.domain.DbmsType;
import com.radiant.dataConnector.domain.FileDataConnector;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.dataConnector.domain.MinioDataConnector;
import com.radiant.dataConnector.domain.dto.CliRequest;
import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.dataConnector.domain.dto.ExecuteQueryRequest;
import com.radiant.dataConnector.domain.repository.DataConnectorRepository;
import com.radiant.dataConnector.domain.repository.DbmsTypeRepository;
import com.radiant.exception.dataConnector.DataConnectorInUse;
import com.radiant.exception.dataConnector.DataConnectorInUseByFileAccess;
import com.radiant.exception.dataConnector.DuplicateDataConnectorName;
import com.radiant.exception.dataConnector.NoSuchDataConnectorException;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.domain.repository.FileAccessPathRepository;
import com.radiant.fileAccess.service.FileListFetcherVisitor;
import com.radiant.fileAccess.service.S3TestConnectionVisitor;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.log.service.service.StatisticService;
import com.radiant.query.domain.DssQueryImplDataConnectorRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@ParametersAreNonnullByDefault
public class DataConnectorServiceImpl implements DataConnectorService {
   private static final Logger LOG = LoggerFactory.getLogger(DataConnectorServiceImpl.class);
   public static final List<DataConnectorType> JDBC_TYPES;
   @Autowired
   private DbAccessService accessService;
   @Autowired
   private DataConnectorRepository repository;
   @Autowired
   private DbmsTypeRepository dbmsTypeRepository;
   @Autowired
   private DssCourtService dssCourtService;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private DssQueryImplDataConnectorRepository dssQueryImplDataConnectorRepository;
   @Autowired
   private StatisticService statisticService;
   @Autowired
   FileAccessPathRepository fileAccessPathRepository;

   public @NotNull DataConnectorDto get(Long id) {
      return new DataConnectorDto(this.getDataConnector(id));
   }

   public @NotNull List<DataConnectorDto> getAll() {
      return (List)this.repository.findAllByArchivedIsFalse().stream().map(DataConnectorDto::new).collect(Collectors.toList());
   }

   public @NotNull DataConnectorDto create(DataConnectorDto request, MultipartFile jarFile) {
      DataConnector connector = request.getType().makeNewConnector();
      if (connector instanceof JdbcDataConnector) {
         DbmsType dbmsType = (DbmsType)this.dbmsTypeRepository.findById(request.getType()).orElseThrow(() -> new IllegalStateException("Not provided DBMS logic for the type " + request.getType()));
         connector = new JdbcDataConnector();
         ((JdbcDataConnector)connector).setDbmsType(dbmsType);
      }

      DataConnectorDto dataConnectorDto = new DataConnectorDto(this.save(this.updateDataConnectorFromRequest(connector, request, jarFile)));
      this.updateCourtList(connector, request);
      this.auditLogService.created((User)this.currentUser.get(), connector).logMessage("Data connector {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
      return dataConnectorDto;
   }

   public @NotNull DataConnectorDto patch(Long id, DataConnectorDto request, MultipartFile jarFile) {
      DataConnector connector = this.getDataConnector(id);
      boolean isDeleting = !connector.getArchived() && request.getArchived() != null && request.getArchived();
      if (isDeleting && this.dssQueryImplDataConnectorRepository.existsByDataConnectorsIn(Collections.singletonList(connector))) {
         throw new DataConnectorInUse(connector.getName());
      } else if (isDeleting && this.fileAccessPathRepository.findByConnector(connector).isPresent()) {
         throw new DataConnectorInUseByFileAccess(connector.getName());
      } else {
         DataConnectorDto dataConnectorDto = new DataConnectorDto(this.save(this.patchDataConnectorFromRequest(connector, request)));
         this.updateCourtList(connector, request);
         if (isDeleting) {
            this.auditLogService.deleted((User)this.currentUser.get(), connector.toAuditObject()).logMessage("Data connector {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});
         } else {
            this.auditLogService.updated((User)this.currentUser.get(), connector).logMessage("Data connector {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
         }

         return dataConnectorDto;
      }
   }

   public @NotNull DataConnectorDto update(Long id, DataConnectorDto request, MultipartFile jarFile) {
      DataConnector connector = this.getDataConnector(id);
      DataConnectorDto dataConnectorDto = new DataConnectorDto(this.save(this.updateDataConnectorFromRequest(connector, request, jarFile)));
      this.updateCourtList(connector, request);
      this.auditLogService.updated((User)this.currentUser.get(), connector).logMessage("Data connector {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return dataConnectorDto;
   }

   private String jdbcConnectorIdent(DataConnector jdbcDataConnector) {
      JdbcDataConnector connector = (JdbcDataConnector)jdbcDataConnector;
      return String.format("%s %s(%s)", connector.getDbmsType().getDisplayName(), connector.getName(), connector.getId());
   }

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public String executeQueryForSingleString(Long id, ExecuteQueryRequest request) {
      DataConnector connector = this.getDataConnector(id);
      if (connector instanceof JdbcDataConnector) {
         return (String)this.statisticService.executeWithTimer(() -> this.accessService.executeQueryForSingleString((JdbcDataConnector)connector, request.getSqlQuery()), "query '%s' on %s", new Object[]{request.getSqlQuery(), this.jdbcConnectorIdent(connector)});
      } else {
         throw new NotImplementedException("Can execute queries only for jdbc data connectors");
      }
   }

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public JSONArray executeQuery(DataConnector dataConnector, ExecuteQueryRequest request) {
      if (dataConnector instanceof JdbcDataConnector) {
         return (JSONArray)this.statisticService.executeWithTimer(() -> this.accessService.executeQuery((JdbcDataConnector)dataConnector, request.getSqlQuery()), "query '%s' on %s", new Object[]{request.getSqlQuery(), this.jdbcConnectorIdent(dataConnector)});
      } else {
         throw new NotImplementedException("Can execute queries only for jdbc data connectors");
      }
   }

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public JSONObject executeQuery(DataConnector dataConnector, ExecuteQueryRequest request, Integer pageSize, Integer pageNum) {
      if (dataConnector instanceof JdbcDataConnector) {
         return (JSONObject)this.statisticService.executeWithTimer(() -> this.accessService.executeQuery((JdbcDataConnector)dataConnector, request.getSqlQuery(), pageSize, pageNum), "query '%s' on %s", new Object[]{request.getSqlQuery(), this.jdbcConnectorIdent(dataConnector)});
      } else {
         throw new NotImplementedException("Can execute queries only for jdbc data connectors");
      }
   }

   public @NotNull DataConnector getDataConnector(Long id) {
      return (DataConnector)this.repository.findByIdAndArchivedIsFalse(id).orElseThrow(() -> new NoSuchDataConnectorException(id));
   }

   public @NotNull List<DataConnector> getDataConnectors(Collection<Long> ids) {
      return this.repository.findByIdInAndArchivedIsFalse(ids);
   }

   public boolean testConnect(DataConnectorDto request) throws Exception {
      if (request.getType() == null) {
         LOG.error("Test connector type must not be empty");
         return false;
      } else {
         FileAccessPath fileAccessPath = new FileAccessPath();
         fileAccessPath.setLogicalPath("./");
         fileAccessPath.setPhysicalPath("./");
         switch (request.getType()) {
            case POSTGRESQL:
            case MYSQL:
            case DAMENG:
               DbmsType dbmsType = (DbmsType)this.dbmsTypeRepository.findById(request.getType()).orElseThrow(() -> new IllegalStateException("Not provided DBMS logic for the type " + request.getType()));
               JdbcDataConnector jdbcDc = request.getId() != null ? (JdbcDataConnector)this.getDataConnector(request.getId()) : new JdbcDataConnector();
               jdbcDc.setDbmsType(dbmsType);
               this.updateDataConnectorFromRequest(jdbcDc, request, (MultipartFile)null);
               return this.accessService.testConnection(jdbcDc);
            case LOCAL_FILE_SYSTEM:
               LocalFileSystemDataConnector localFs = request.getId() != null ? (LocalFileSystemDataConnector)this.getDataConnector(request.getId()) : new LocalFileSystemDataConnector();
               this.updateDataConnectorFromRequest(localFs, request, (MultipartFile)null);
               localFs.accept(new FileListFetcherVisitor(fileAccessPath, "./"));
               return true;
            case MINIO:
               MinioDataConnector minioDc = request.getId() != null ? (MinioDataConnector)this.getDataConnector(request.getId()) : new MinioDataConnector();
               this.updateDataConnectorFromRequest(minioDc, request, (MultipartFile)null);
               return minioDc.testConnection(new S3TestConnectionVisitor(request, fileAccessPath));
            default:
               throw new IllegalStateException("Unexpected data connector type: " + request.getType());
         }
      }
   }

   public boolean testConnect(Long connectorId) throws Exception {
      DataConnectorDto request = new DataConnectorDto(this.getDataConnector(connectorId));
      request.setId(connectorId);
      return this.testConnect(request);
   }

   @Transactional(
      propagation = Propagation.NOT_SUPPORTED
   )
   public String executeCliQuery(CliRequest request) {
      DataConnector connector = this.repository.getByNameAndArchivedIsFalse(request.getConnectorName());
      if (connector == null) {
         throw new NoSuchDataConnectorException(request.getConnectorName());
      } else {
         ExecuteQueryRequest sqlQuery = new ExecuteQueryRequest();
         sqlQuery.setSqlQuery(request.getQuery());
         return this.executeQuery(connector, sqlQuery).toString();
      }
   }

   public List<DataConnector> getAllConnectors() {
      return this.repository.findAllByArchivedIsFalse();
   }

   private @NotNull DataConnector patchDataConnectorFromRequest(DataConnector connector, DataConnectorDto request) {
      UpdateEntityHelper.ifNotNull(request.getName(), connector::setName);
      UpdateEntityHelper.ifNotNull(request.getLive(), connector::setLive);
      UpdateEntityHelper.ifNotNull(request.getArchive(), connector::setArchive);
      UpdateEntityHelper.ifNotNull(request.getArchived(), connector::setArchived);
      UpdateEntityHelper.ifNotNull(request.getDescription(), connector::setDescription);
      if (connector instanceof JdbcDataConnector) {
         JdbcDataConnector jdbcConnector = (JdbcDataConnector)connector;
         UpdateEntityHelper.ifNotNull(request.getHostname(), jdbcConnector::setHostname);
         UpdateEntityHelper.ifNotNull(request.getPort(), jdbcConnector::setPort);
         UpdateEntityHelper.ifNotNull(request.getDbName(), jdbcConnector::setDbName);
         UpdateEntityHelper.ifNotNull(request.getUserId(), jdbcConnector::setUserId);
         UpdateEntityHelper.ifNotNull(request.getUserPassword(), jdbcConnector::setUserPassword);
         UpdateEntityHelper.ifNotNull(request.getCustomJdbcUrl(), jdbcConnector::setCustomJdbcUrl);
         if (jdbcConnector.getDbVersion() == null) {
            jdbcConnector.setDbVersion(this.accessService.getVersion(jdbcConnector));
         }
      }

      return connector;
   }

   private @NotNull DataConnector updateDataConnectorFromRequest(DataConnector connector, DataConnectorDto request, @Nullable MultipartFile jarFile) {
      connector.setName(request.getName());
      connector.setLive(request.getLive());
      connector.setArchive(request.getArchive());
      connector.setArchived(request.getArchived());
      connector.setDescription(request.getDescription());
      return (DataConnector)connector.accept(new UpdateByRequestVisitor(request, jarFile));
   }

   private void updateCourtList(DataConnector connector, DataConnectorDto request) {
      List<DssHostedCourt> hostedCourts = new ArrayList();
      if (!CollectionUtils.isEmpty(request.getHostedCourts())) {
         for(Long courtId : request.getHostedCourts()) {
            hostedCourts.add(this.dssCourtService.getHostedCourtByCourt(courtId));
         }
      }

      this.dssCourtService.deleteCourtStoresByDataConnector(connector);
      this.dssCourtService.linkCourtsWithDataConnector(hostedCourts, connector);
   }

   private @NotNull DataConnector save(DataConnector connector) {
      Boolean exists = connector.getId() == null ? this.repository.existsByNameAndArchivedIsFalse(connector.getName()) : this.repository.existsByNameAndArchivedIsFalseAndIdNot(connector.getName(), connector.getId());
      if (exists) {
         throw new DuplicateDataConnectorName(connector.getName());
      } else {
         return (DataConnector)this.repository.saveAndFlush(connector);
      }
   }

   static {
      JDBC_TYPES = ImmutableList.of(DataConnectorType.POSTGRESQL, DataConnectorType.MYSQL, DataConnectorType.DAMENG);
   }

   private class UpdateByRequestVisitor implements DataConnectorVisitor<DataConnector> {
      private final DataConnectorDto request;
      private final MultipartFile jarFile;

      UpdateByRequestVisitor(DataConnectorDto request, @Nullable MultipartFile jarFile) {
         this.request = request;
         this.jarFile = jarFile;
      }

      public DataConnector visit(JdbcDataConnector jdbcDataConnector) {
         jdbcDataConnector.setHostname(this.request.getHostname());
         jdbcDataConnector.setDbName(this.request.getDbName());
         jdbcDataConnector.setPort(this.request.getPort());
         jdbcDataConnector.setUserId(this.request.getUserId());
         UpdateEntityHelper.ifNotNull(this.request.getUserPassword(), jdbcDataConnector::setUserPassword);
         UpdateEntityHelper.ifNotNull(this.request.getCustomJdbcUrl(), jdbcDataConnector::setCustomJdbcUrl);
         if (jdbcDataConnector.getDbVersion() == null) {
            jdbcDataConnector.setDbVersion(DataConnectorServiceImpl.this.accessService.getVersion(jdbcDataConnector));
         }

         return jdbcDataConnector;
      }

      public FileDataConnector visit(LocalFileSystemDataConnector localFSDataConnector) {
         return localFSDataConnector;
      }

      public FileDataConnector visit(MinioDataConnector minioDataConnector) {
         minioDataConnector.setEndpoint(this.request.getEndpoint());
         minioDataConnector.setAccessKeyId(this.request.getAccessKeyId());
         UpdateEntityHelper.ifNotNull(this.request.getAccessKeySecret(), minioDataConnector::setAccessKeySecret);
         minioDataConnector.setBucketName(this.request.getBucketName());
         return minioDataConnector;
      }
   }
}
