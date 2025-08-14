package com.radiant.auth;

import com.radiant.auth.domain.AccessKeyAuthRequest;
import com.radiant.auth.domain.AccessKeyAuthResponse;
import com.radiant.auth.service.AccessKeyAuthenticationService;
import com.radiant.fileAccess.JudgePortalController;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.judgePortal.dto.JudgePortalResponse;
import com.radiant.restapi.RestApiUtils;
import io.swagger.annotations.Api;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(
   tags = {"Authentication operations (login): GDDS specific operations"}
)
public class GddsAuthenticationController extends JudgePortalController {
   @Autowired
   private AccessKeyAuthenticationService accessKeyAuthenticationService;
   @Autowired
   private I18nService i18n;

   @PostMapping(
      path = {"/api/public/v1/login/app", "/radiant/rdsm/api/public/v1/login/app"}
   )
   public JudgePortalResponse<AccessKeyAuthResponse> authViaAccessKey(@RequestBody @Valid AccessKeyAuthRequest authenticationRequest, HttpServletRequest request) {
      String ipAddress = RestApiUtils.getRequesterIp(request);
      AccessKeyAuthResponse response = this.accessKeyAuthenticationService.createAuthResponse(authenticationRequest, ipAddress);
      return JudgePortalUtil.jpResponse(response, HttpStatus.OK, this.i18n.message("success"), (String)null);
   }
}
