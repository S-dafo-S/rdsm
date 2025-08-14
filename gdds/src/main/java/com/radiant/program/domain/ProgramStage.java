package com.radiant.program.domain;

import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import javax.servlet.http.HttpServletRequest;

public abstract class ProgramStage {
   private ProgramStage nextStage;

   abstract void handleRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest);

   public void receiveRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest) {
      this.handleRequest(request, response, originalRequest);
      if (this.nextStage != null) {
         this.nextStage.receiveRequest(request, response, originalRequest);
      }

   }

   public ProgramStage getNextStage() {
      return this.nextStage;
   }

   public void setNextStage(final ProgramStage nextStage) {
      this.nextStage = nextStage;
   }
}
