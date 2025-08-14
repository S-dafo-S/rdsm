package com.radiant.account;

import com.radiant.account.domain.User;
import com.radiant.account.service.AccountService;
import com.radiant.auth.domain.AuthenticationMethod;
import com.radiant.auth.domain.Session;
import com.radiant.auth.service.CurrentUser;
import com.radiant.auth.service.SessionService;
import com.radiant.i18n.I18nService;
import com.radiant.judgePortal.JudgePortalUtil;
import com.radiant.judgePortal.dto.JudgePortalResponse;
import io.swagger.annotations.Api;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/api/public/v1/user"})
@RestController
@Api(
   tags = {"Public user controller"}
)
public class PublicUserController {
   @Autowired
   private CurrentUser currentUser;
   @Autowired
   private SessionService sessionService;
   @Autowired
   private AccountService accountService;
   @Autowired
   private I18nService i18n;

   @GetMapping(
      value = {"/me"},
      produces = {"application/json"}
   )
   public JudgePortalResponse<JudgePortalUserDto> getCurrentUser(HttpServletRequest request) {
      User user = (User)this.currentUser.get();
      AuthenticationMethod authMethod = (AuthenticationMethod)this.sessionService.getSession(request).map(Session::getAuthenticationMethod).orElse((Object)null);
      return JudgePortalUtil.jpResponse(new JudgePortalUserDto(this.accountService.getAuthedUser(user, authMethod)), HttpStatus.OK, this.i18n.message("success"), (String)null);
   }
}
