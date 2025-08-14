package com.radiant.dataConnector;

import com.radiant.dataConnector.domain.dto.DataConnectorDto;
import com.radiant.dataConnector.domain.dto.ExecuteQueryRequest;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.securityAnnotation.IsDataManager;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping({"/api/internal/v1/data-connector"})
@RestController
@Api(
   tags = {"Data connector operations"}
)
public class DataConnectorController {
   @Autowired
   private DataConnectorService service;

   @IsDataManager
   @GetMapping
   public List<DataConnectorDto> getAll() {
      return this.service.getAll();
   }

   @IsDataManager
   @GetMapping({"/{id}"})
   public DataConnectorDto get(@PathVariable("id") Long id) {
      return this.service.get(id);
   }

   @IsDataManager
   @PostMapping(
      consumes = {"multipart/form-data"}
   )
   @ResponseStatus(HttpStatus.CREATED)
   public DataConnectorDto create(@RequestPart("request") @Valid DataConnectorDto request, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.service.create(request, jarFile);
   }

   @IsDataManager
   @PostMapping(
      consumes = {"application/json"}
   )
   @ResponseStatus(HttpStatus.CREATED)
   public DataConnectorDto create(@RequestBody @Valid DataConnectorDto request) {
      return this.service.create(request, (MultipartFile)null);
   }

   @IsDataManager
   @PatchMapping(
      value = {"/{id}"},
      consumes = {"multipart/form-data"}
   )
   public DataConnectorDto patch(@PathVariable("id") Long id, @RequestPart("request") @Valid DataConnectorDto request, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.service.patch(id, request, jarFile);
   }

   @IsDataManager
   @PatchMapping(
      value = {"/{id}"},
      consumes = {"application/json"}
   )
   public DataConnectorDto patch(@PathVariable("id") Long id, @RequestBody @Valid DataConnectorDto request) {
      return this.service.patch(id, request, (MultipartFile)null);
   }

   @IsDataManager
   @PutMapping(
      value = {"/{id}"},
      consumes = {"multipart/form-data"}
   )
   public DataConnectorDto update(@PathVariable("id") Long id, @RequestPart @Valid DataConnectorDto request, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.service.update(id, request, jarFile);
   }

   @IsDataManager
   @PutMapping(
      value = {"/{id}"},
      consumes = {"application/json"}
   )
   public DataConnectorDto update(@PathVariable("id") Long id, @RequestBody @Valid DataConnectorDto request) {
      return this.service.update(id, request, (MultipartFile)null);
   }

   @IsDataManager
   @PostMapping({"/{id}/query"})
   public String executeSql(@PathVariable("id") Long id, @RequestBody @Valid ExecuteQueryRequest request) {
      return this.service.executeQueryForSingleString(id, request);
   }

   @IsDataManager
   @PutMapping({"/test"})
   public Boolean testConnect(@RequestBody @Valid DataConnectorDto request) throws Exception {
      return this.service.testConnect(request);
   }
}
