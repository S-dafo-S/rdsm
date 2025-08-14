package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;

public class JudgePortalBaseResponse {
   private @NotNull Integer code;
   private @NotNull Boolean success;
   @JsonInclude(Include.NON_NULL)
   @Nullable
   private String errorCode;
   private @NotNull String msg;
   private @NotNull String message;

   public JudgePortalBaseResponse(HttpStatus code, @Nullable String errorCode, String msg) {
      this.code = code.value();
      this.errorCode = errorCode;
      this.success = code.series() == Series.SUCCESSFUL;
      this.msg = msg;
      this.message = msg;
   }

   public Integer getCode() {
      return this.code;
   }

   public Boolean getSuccess() {
      return this.success;
   }

   @Nullable
   public String getErrorCode() {
      return this.errorCode;
   }

   public String getMsg() {
      return this.msg;
   }

   public String getMessage() {
      return this.message;
   }

   public void setCode(final Integer code) {
      this.code = code;
   }

   public void setSuccess(final Boolean success) {
      this.success = success;
   }

   public void setErrorCode(@Nullable final String errorCode) {
      this.errorCode = errorCode;
   }

   public void setMsg(final String msg) {
      this.msg = msg;
   }

   public void setMessage(final String message) {
      this.message = message;
   }

   public JudgePortalBaseResponse() {
   }
}
