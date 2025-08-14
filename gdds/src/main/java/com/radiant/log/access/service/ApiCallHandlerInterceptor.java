package com.radiant.log.access.service;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiCallHandlerInterceptor implements HandlerInterceptor {
   public static final String REQUEST_TIME = "startTime";

   public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
      request.setAttribute("startTime", (new Date()).getTime());
      return true;
   }
}
