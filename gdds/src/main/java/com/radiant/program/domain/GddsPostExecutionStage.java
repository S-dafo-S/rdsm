package com.radiant.program.domain;

import com.radiant.program.QnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import javax.servlet.http.HttpServletRequest;

public class GddsPostExecutionStage extends ProgramStage {
   private QnodeProgramAdapter gddsProgram;

   public GddsPostExecutionStage(QnodeProgramAdapter gddsProgram) {
      this.gddsProgram = gddsProgram;
   }

   void handleRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest) {
      this.gddsProgram.postQnodeExecute(request, response);
   }
}
