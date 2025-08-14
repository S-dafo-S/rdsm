package com.radiant.fileAccess.path;

import com.radiant.fileAccess.path.dto.DataConnectorFilePathsDto;
import com.radiant.fileAccess.path.dto.FileAccessPathsBatchCreateRequest;
import com.radiant.fileAccess.path.service.DataConnectorFileAccessPathsService;
import com.radiant.securityAnnotation.IsDataManager;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/api/internal/v1/data-connector"})
@RestController
@Api(
   tags = {"File access paths operations"}
)
public class FileDataConnectorAccessPathsController {
   @Autowired
   private DataConnectorFileAccessPathsService service;

   @IsDataManager
   @GetMapping({"/file-access-path"})
   public List<DataConnectorFilePathsDto> getAll() {
      return this.service.getAll();
   }

   @IsDataManager
   @GetMapping({"/{id}/file-access-path"})
   public DataConnectorFilePathsDto get(@PathVariable("id") Long id) {
      return this.service.get(id);
   }

   @IsDataManager
   @PostMapping({"/file-access-path"})
   public DataConnectorFilePathsDto appendToConnectorPaths(@RequestBody @Valid FileAccessPathsBatchCreateRequest request) {
      return this.service.appendToConnectorPaths(request);
   }

   @IsDataManager
   @PutMapping({"/file-access-path"})
   public DataConnectorFilePathsDto overrideConnectorPaths(@RequestBody @Valid FileAccessPathsBatchCreateRequest request) {
      return this.service.overrideConnectorPaths(request);
   }

   @IsDataManager
   @DeleteMapping({"/{id}/file-access-path"})
   public DataConnectorFilePathsDto deleteConnectorPaths(@PathVariable("id") Long id) {
      return this.service.deleteConnectorPaths(id);
   }
}
