package com.radiant.fileAccess.service;

import com.radiant.account.domain.User;
import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.auth.service.CurrentUser;
import com.radiant.exception.fileAccess.DuplicateFileAccessProgramName;
import com.radiant.exception.fileAccess.NoSuchFileAccessProgramException;
import com.radiant.fileAccess.GddsFileAccessProgram;
import com.radiant.fileAccess.dto.FileAccessProgramDto;
import com.radiant.fileAccess.repository.GddsFileAccessProgramRepository;
import com.radiant.log.audit.service.AuditLogService;
import com.radiant.util.DBUtils;
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
public class FileAccessProgramServiceImpl implements FileAccessProgramService {
   @Autowired
   private GddsFileAccessProgramRepository repository;
   @Autowired
   private AuditLogService auditLogService;
   @Autowired
   private CurrentUser currentUser;

   public @NotNull FileAccessProgramDto get(Long id) {
      return new FileAccessProgramDto(this.getFileAccessProgram(id));
   }

   public @NotNull List<FileAccessProgramDto> getAll() {
      return (List)this.repository.findAll().stream().map(FileAccessProgramDto::new).collect(Collectors.toList());
   }

   public @NotNull FileAccessProgramDto patch(Long id, FileAccessProgramDto request) {
      GddsFileAccessProgram program = this.getFileAccessProgram(id);
      this.save(this.patchFileAccessFromRequest(program, request));
      this.auditLogService.updated((User)this.currentUser.get(), program).logMessage("File access {0} is updated", new Object[]{AuditLogService.TOP_OBJECT});
      return new FileAccessProgramDto(program);
   }

   private @NotNull GddsFileAccessProgram patchFileAccessFromRequest(GddsFileAccessProgram program, FileAccessProgramDto request) {
      UpdateEntityHelper.ifNotNull(request.getStatus(), program::setStatus);
      return program;
   }

   private @NotNull GddsFileAccessProgram getFileAccessProgram(Long id) {
      return (GddsFileAccessProgram)this.repository.findById(id).orElseThrow(() -> new NoSuchFileAccessProgramException(id));
   }

   private @NotNull GddsFileAccessProgram save(GddsFileAccessProgram program) {
      try {
         return (GddsFileAccessProgram)this.repository.saveAndFlush(program);
      } catch (DataIntegrityViolationException e) {
         if (DBUtils.isConstraintViolated(e, "file_access_program_name_uniq")) {
            throw new DuplicateFileAccessProgramName(program.getName());
         } else {
            throw e;
         }
      }
   }
}
