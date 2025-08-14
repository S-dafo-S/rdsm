package com.radiant.fileAccess.path.service;

import com.google.common.collect.ImmutableList;
import com.radiant.account.domain.User;
import com.radiant.auth.service.CurrentUser;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.exception.fileAccess.DuplicateFileAccessPaths;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.domain.FileAccessPathStatus;
import com.radiant.fileAccess.path.domain.repository.FileAccessPathRepository;
import com.radiant.fileAccess.path.dto.DataConnectorFilePathsDto;
import com.radiant.fileAccess.path.dto.FileAccessPathMapping;
import com.radiant.fileAccess.path.dto.FileAccessPathsBatchCreateRequest;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.util.DBUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@ParametersAreNonnullByDefault
public class DataConnectorFileAccessPathServiceImpl implements DataConnectorFileAccessPathsService {
   @Autowired
   private DataConnectorService connectorService;
   @Autowired
   private FileAccessPathRepository repository;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;

   public List<DataConnectorFilePathsDto> getAll() {
      List<FileAccessPath> paths = this.repository.findAll();
      Map<Long, List<FileAccessPath>> connectorPathsMap = (Map)paths.stream().map((path) -> path.getConnector().getId()).distinct().collect(Collectors.toMap(Function.identity(), (connectorId) -> this.filterPaths(paths, connectorId)));
      List<DataConnector> dataConnectors = this.connectorService.getDataConnectors(connectorPathsMap.keySet());
      return (List)dataConnectors.stream().map((connector) -> new DataConnectorFilePathsDto(connector, (List)connectorPathsMap.getOrDefault(connector.getId(), ImmutableList.of()))).collect(Collectors.toList());
   }

   public DataConnectorFilePathsDto get(Long connectorId) {
      DataConnector dataConnector = this.connectorService.getDataConnector(connectorId);
      return new DataConnectorFilePathsDto(dataConnector);
   }

   public DataConnectorFilePathsDto appendToConnectorPaths(FileAccessPathsBatchCreateRequest request) {
      DataConnector connector = this.connectorService.getDataConnector(request.getConnectorId());
      List<FileAccessPath> newPaths = this.generatePaths(connector, request);
      connector.getFileAccessPaths().addAll(newPaths);
      this.save(newPaths);
      this.auditLogService.updated((User)this.currentUser.get(), connector).logMessage("File path list is updated for data connector {0}", new Object[]{AuditLogService.TOP_OBJECT});
      return new DataConnectorFilePathsDto(connector);
   }

   public DataConnectorFilePathsDto overrideConnectorPaths(FileAccessPathsBatchCreateRequest request) {
      DataConnector connector = this.connectorService.getDataConnector(request.getConnectorId());
      List<FileAccessPath> oldPaths = new ArrayList(connector.getFileAccessPaths());
      List<FileAccessPath> newPaths = this.generatePaths(connector, request);
      connector.getFileAccessPaths().clear();
      connector.setFileAccessPaths(newPaths);
      this.repository.deleteAllInBatch(oldPaths);
      this.repository.flush();
      this.save(newPaths);
      this.auditLogService.updated((User)this.currentUser.get(), connector).logMessage("File path list is updated for data connector {0}", new Object[]{AuditLogService.TOP_OBJECT});
      return new DataConnectorFilePathsDto(connector);
   }

   public DataConnectorFilePathsDto deleteConnectorPaths(Long connectorId) {
      DataConnector connector = this.connectorService.getDataConnector(connectorId);
      List<FileAccessPath> oldPaths = new ArrayList(connector.getFileAccessPaths());
      connector.getFileAccessPaths().clear();
      this.repository.deleteAllInBatch(oldPaths);
      this.repository.flush();
      this.auditLogService.updated((User)this.currentUser.get(), connector).logMessage("File path list is updated for data connector {0}", new Object[]{AuditLogService.TOP_OBJECT});
      return new DataConnectorFilePathsDto(connector);
   }

   private @NotNull List<FileAccessPath> generatePaths(DataConnector connector, FileAccessPathsBatchCreateRequest request) {
      return (List)request.getPaths().stream().map((pathRequest) -> this.entityFromRequestAndMapping(connector, request, pathRequest)).collect(Collectors.toList());
   }

   private @NotNull FileAccessPath entityFromRequestAndMapping(DataConnector connector, FileAccessPathsBatchCreateRequest request, FileAccessPathMapping pathRequest) {
      FileAccessPath path = new FileAccessPath();
      path.setConnector(connector);
      path.setLogicalPath(pathRequest.getLogicalPath());
      path.setPhysicalPath(pathRequest.getPhysicalPath());
      path.setStatus(FileAccessPathStatus.FULLY_OPERABLE);
      path.setUserId(request.getUserId());
      path.setUserPassword(request.getUserPassword());
      path.setDescription(request.getDescription());
      return path;
   }

   private @NotNull List<FileAccessPath> filterPaths(List<FileAccessPath> paths, Long connectorId) {
      return (List)paths.stream().filter((path) -> Objects.equals(path.getConnector().getId(), connectorId)).collect(Collectors.toList());
   }

   private void save(List<FileAccessPath> newPaths) {
      try {
         this.repository.saveAllAndFlush(newPaths);
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "logical_path_uniq")) {
            throw new DuplicateFileAccessPaths(newPaths.toString());
         } else {
            throw e;
         }
      }
   }
}
