package com.radiant.fileAccess.path.service;

import com.radiant.account.domain.User;
import com.radiant.account.exception.NotFoundException;
import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.auth.service.CurrentUser;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.exception.fileAccess.CanNotMapToPhysicalPathException;
import com.radiant.exception.fileAccess.DuplicateFileAccessPaths;
import com.radiant.exception.fileAccess.NoSuchFileAccessPathException;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.domain.FileAccessPathStatus;
import com.radiant.fileAccess.path.domain.repository.FileAccessPathRepository;
import com.radiant.fileAccess.path.dto.FileAccessApiDto;
import com.radiant.fileAccess.path.dto.FileAccessPathCreateRequest;
import com.radiant.fileAccess.path.dto.FileAccessPathDto;
import com.radiant.gddsConnect.service.GddsConnectService;
import com.radiant.log.audit.domain.AuditObject;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.util.DBUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
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
public class FileAccessPathServiceImpl implements FileAccessPathService {
   @Autowired
   private DataConnectorService connectorService;
   @Autowired
   private GddsConnectService gddsConnectService;
   @Autowired
   private FileAccessPathRepository repository;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;

   @Transactional(
      readOnly = true
   )
   public @NotNull FileAccessPathDto get(Long id) {
      return new FileAccessPathDto(this.getFileAccessPath(id));
   }

   @Transactional(
      readOnly = true
   )
   public @NotNull FileAccessApiDto findByLogicalPath(String logicalPath) {
      return new FileAccessApiDto((FileAccessPath)this.repository.findByLogicalPath(logicalPath).orElseThrow(() -> new NotFoundException(logicalPath)), this.gddsUrl(), this.dssUrl());
   }

   @Transactional(
      readOnly = true
   )
   public @NotNull List<FileAccessPathDto> getAll() {
      return (List)this.repository.findAll().stream().map(FileAccessPathDto::new).collect(Collectors.toList());
   }

   public @NotNull FileAccessPathDto create(FileAccessPathCreateRequest request) {
      FileAccessPath path = new FileAccessPath();
      path.setStatus(FileAccessPathStatus.FULLY_OPERABLE);
      path.setUserPassword(request.getUserPassword());
      this.save(this.updateFileAccessPathFromRequest(path, request));
      this.auditLogService.created((User)this.currentUser.get(), path).logMessage("File access path {0} is created", new Object[]{AuditLogService.TOP_OBJECT});
      return new FileAccessPathDto(path);
   }

   public @NotNull FileAccessPathDto update(Long id, FileAccessPathCreateRequest request) {
      FileAccessPath path = this.getFileAccessPath(id);
      UpdateEntityHelper.ifNotNull(request.getUserPassword(), path::setUserPassword);
      this.save(this.updateFileAccessPathFromRequest(path, request));
      this.auditLogService.updated((User)this.currentUser.get(), path).logMessage("File access path {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new FileAccessPathDto(path);
   }

   public void delete(Long id) {
      this.repository.findById(id).ifPresent((path) -> {
         AuditObject auditObject = path.toAuditObject();
         this.repository.delete(path);
         this.auditLogService.deleted((User)this.currentUser.get(), auditObject).logMessage("File access path {0} is deleted", new Object[]{AuditLogService.TOP_OBJECT});
      });
   }

   @Transactional(
      readOnly = true
   )
   public FileAccessPath findByLogicalPath(Path path) {
      return (FileAccessPath)this.repository.findAllByLogicalPathStartingWith(path.toString()).stream().filter((filePath) -> Paths.get(filePath.getLogicalPath()).startsWith(path)).min(Comparator.comparingInt((filePath) -> Paths.get(filePath.getLogicalPath()).getNameCount())).orElseThrow(() -> new CanNotMapToPhysicalPathException(path.toString()));
   }

   @Transactional(
      readOnly = true
   )
   public FileAccessPath findByLogicalPathAndConnector(Path path, List<DataConnector> dataConnectors) {
      return (FileAccessPath)this.repository.findByConnectorInAndLogicalPathStartingWith(dataConnectors, path.toString()).stream().filter((filePath) -> Paths.get(filePath.getLogicalPath()).startsWith(path)).min(Comparator.comparingInt((filePath) -> Paths.get(filePath.getLogicalPath()).getNameCount())).orElseThrow(() -> new CanNotMapToPhysicalPathException(path.toString()));
   }

   private @NotNull FileAccessPath updateFileAccessPathFromRequest(FileAccessPath path, FileAccessPathCreateRequest request) {
      path.setConnector(this.connectorService.getDataConnector(request.getConnectorId()));
      path.setDescription(request.getDescription());
      path.setPhysicalPath(request.getPhysicalPath());
      path.setLogicalPath(request.getLogicalPath());
      path.setDescription(request.getDescription());
      path.setUserId(request.getUserId());
      return path;
   }

   private @NotNull FileAccessPath getFileAccessPath(Long id) {
      return (FileAccessPath)this.repository.findById(id).orElseThrow(() -> new NoSuchFileAccessPathException(id));
   }

   private @NotNull FileAccessPath save(FileAccessPath path) {
      try {
         return (FileAccessPath)this.repository.saveAndFlush(path);
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "logical_path_uniq")) {
            throw new DuplicateFileAccessPaths(path.getLogicalPath());
         } else {
            throw e;
         }
      }
   }

   private String gddsUrl() {
      return this.gddsConnectService.getConnectInfo().getGddsUrl();
   }

   private String dssUrl() {
      return this.gddsConnectService.getConnectInfo().getDssUrl();
   }
}
