package com.radiant.integrationFunction;

import com.radiant.integrationFunction.domain.dto.IntegrationFunctionDto;
import com.radiant.integrationFunction.service.IntegrationFunctionService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping({"/api/internal/v1/integration-function"})
@RestController
@Api(
   tags = {"Integration function operations"}
)
public class IntegrationFunctionController {
   @Autowired
   private IntegrationFunctionService integrationFunctionService;

   @IsDataManager
   @GetMapping
   public List<IntegrationFunctionDto> getAll() {
      return this.integrationFunctionService.getAll();
   }

   @IsDataManager
   @PostMapping
   public IntegrationFunctionDto create(@RequestPart @Valid IntegrationFunctionDto request, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.integrationFunctionService.createAndGet(request, jarFile);
   }

   @IsDataManager
   @PutMapping({"/{funcId}"})
   public IntegrationFunctionDto update(@PathVariable("funcId") Long funcId, @RequestPart @Valid IntegrationFunctionDto request, @RequestPart(value = "file",required = false) MultipartFile jarFile) {
      return this.integrationFunctionService.update(funcId, request, jarFile);
   }

   @IsDataManager
   @ResponseStatus(HttpStatus.NO_CONTENT)
   @DeleteMapping({"/{funcId}"})
   public void delete(@PathVariable("funcId") Long funcId) {
      this.integrationFunctionService.delete(funcId);
   }
}
