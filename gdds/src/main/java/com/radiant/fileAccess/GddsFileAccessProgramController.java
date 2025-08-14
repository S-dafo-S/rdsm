package com.radiant.fileAccess;

import com.radiant.fileAccess.dto.FileAccessProgramDto;
import com.radiant.fileAccess.service.FileAccessProgramService;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/api/internal/v1/file-access-program"})
@RestController
@Api(
   tags = {"Public court management operations"}
)
public class GddsFileAccessProgramController {
   @Autowired
   private FileAccessProgramService service;

   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @GetMapping
   public List<FileAccessProgramDto> getAll() {
      return this.service.getAll();
   }

   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @GetMapping({"/{id}"})
   public FileAccessProgramDto get(@PathVariable("id") Long id) {
      return this.service.get(id);
   }

   @PreAuthorize("hasAnyAuthority('GDDS_DATA_MANAGER')")
   @PatchMapping({"/{id}"})
   public FileAccessProgramDto patch(@PathVariable("id") Long id, @RequestBody @Valid FileAccessProgramDto request) {
      return this.service.patch(id, request);
   }
}
