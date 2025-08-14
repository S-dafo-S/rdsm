package com.radiant.court;

import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.court.domain.dto.GddsCourtListPlain;
import com.radiant.court.domain.dto.GddsHostedCourtListPlain;
import com.radiant.court.domain.dto.VersionedCourtTree;
import com.radiant.court.service.GddsCourtService;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.judgePortal.dto.JudgePortalPage;
import com.radiant.judgePortal.dto.JudgePortalResponse;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/public/v1"})
@Api(
   tags = {"Public court management operations"}
)
public class PublicCourtController {
   @Autowired
   private GddsCourtService gddsCourtService;
   @Autowired
   private I18nService i18n;

   @GetMapping({"/court_tree"})
   public JudgePortalResponse<CourtTreeNode> getCourtTree() {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getCourtTree(false), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_tree_by_region/{regionId}"})
   public JudgePortalResponse<CourtTreeNode> getCourtTreeByRegion(@PathVariable("regionId") Long region) {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getCourtTree(region), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_by_name/{courtName}"})
   public JudgePortalResponse<CourtDto> getCourtByName(@PathVariable("courtName") String courtName) {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getCourtByName(courtName), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_by_partial_name/{courtName}"})
   public JudgePortalResponse<JudgePortalPage<CourtDto>> getCourtByPartialName(@PathVariable("courtName") String courtName) {
      return JudgePortalUtil.jpResponse(JudgePortalUtil.pageable(this.gddsCourtService.getCourtByPartialName(courtName)), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_by_id/{courtId}"})
   public JudgePortalResponse<CourtDto> getCourt(@PathVariable("courtId") Long courtId) {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getById(courtId), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_list"})
   public JudgePortalResponse<VersionedCourtTree> getLatestVersion(@RequestParam(value = "version_greater",required = false) String versionGreater) {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getGreaterCourtTreeVersion(versionGreater, false), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/hosted_court_list"})
   public JudgePortalResponse<VersionedCourtTree> getHostedLatestVersion(@RequestParam(value = "version_greater",required = false) String versionGreater) {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getGreaterCourtTreeVersion(versionGreater, true), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/hosted_court_list_plain"})
   public JudgePortalResponse<GddsHostedCourtListPlain> getHostedCourtPlain() {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getHostedCourtListPlain(), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }

   @GetMapping({"/court_list_plain"})
   public JudgePortalResponse<GddsCourtListPlain> getCourtListPlain() {
      return JudgePortalUtil.jpResponse(this.gddsCourtService.getCourtListPlain(), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }
}
