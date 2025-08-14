package com.radiant.fileAccess.path;

import com.radiant.fileAccess.path.dto.FileAccessApiDto;
import com.radiant.fileAccess.path.dto.FileAccessPathCreateRequest;
import com.radiant.fileAccess.path.dto.FileAccessPathDto;
import com.radiant.fileAccess.path.service.FileAccessPathService;
import com.radiant.securityAnnotation.IsDataManager;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/api/internal/v1/file-access-path"})
@RestController
@Api(
   tags = {"File access path operations"}
)
public class FileAccessPathController {
   @Autowired
   private FileAccessPathService service;

   @IsDataManager
   @GetMapping
   public List<FileAccessPathDto> getAll() {
      return this.service.getAll();
   }

   @IsDataManager
   @GetMapping({"/{id}"})
   public FileAccessPathDto get(@PathVariable("id") Long id) {
      return this.service.get(id);
   }

   @GetMapping(
      value = {"/get-by-logical-path/{logicalPath}"},
      produces = {"application/json"}
   )
   public FileAccessApiDto getByName(@PathVariable("logicalPath") String logicalPath) {
      return this.service.findByLogicalPath(logicalPath);
   }

   @IsDataManager
   @PostMapping
   @ResponseStatus(HttpStatus.CREATED)
   public FileAccessPathDto create(@RequestBody @Valid FileAccessPathCreateRequest request) {
      return this.service.create(request);
   }

   @IsDataManager
   @PutMapping({"/{id}"})
   public FileAccessPathDto update(@PathVariable("id") Long id, @RequestBody @Valid FileAccessPathCreateRequest request) {
      return this.service.update(id, request);
   }

   @IsDataManager
   @DeleteMapping({"/{id}"})
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void delete(@PathVariable("id") Long id) {
      this.service.delete(id);
   }
}
