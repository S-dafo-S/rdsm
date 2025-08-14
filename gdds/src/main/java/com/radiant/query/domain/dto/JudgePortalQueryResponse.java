package com.radiant.query.domain.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import javax.annotation.Nullable;
import org.springframework.http.HttpStatus;

public class JudgePortalQueryResponse extends JudgePortalBaseResponse {
   @JsonRawValue
   @Nullable
   private Object data;

   public JudgePortalQueryResponse(HttpStatus code, @Nullable String errorCode, String msg, @Nullable Object data) {
      super(code, errorCode, msg);
      this.data = data;
   }

   @Nullable
   public Object getData() {
      return this.data;
   }

   public void setData(@Nullable final Object data) {
      this.data = data;
   }

   public JudgePortalQueryResponse() {
   }
}
