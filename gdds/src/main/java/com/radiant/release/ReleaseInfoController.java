package com.radiant.release;

import com.radiant.build.service.ReleaseCompatibilityResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/public/v1/release"})
public class ReleaseInfoController {
   @Autowired
   private ReleaseInfoServiceImpl releaseInfoService;

   @GetMapping({"/compatible"})
   public ResponseEntity<ReleaseCompatibilityResponseDto> isCompatible(@RequestParam("version") String dssVersion) {
      return ResponseEntity.ok(this.releaseInfoService.isCompatible(dssVersion));
   }
}
