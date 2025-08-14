package com.radiant.program.domain;

import com.radiant.program.QnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import javax.servlet.http.HttpServletRequest;

public class GddsPreExecutionStage extends ProgramStage {
   private QnodeProgramAdapter gddsProgram;

   public GddsPreExecutionStage(QnodeProgramAdapter gddsProgram) {
      this.gddsProgram = gddsProgram;
   }

   void handleRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest) {
      this.gddsProgram.preQnodeExecute(request, response);
   }
}
