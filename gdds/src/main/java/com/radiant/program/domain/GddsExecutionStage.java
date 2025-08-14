package com.radiant.program.domain;

import com.radiant.exception.program.GddsExecuteNotImplemented;
import com.radiant.program.QnodeProgramAdapter;
import com.radiant.program.dto.ProgramRequest;
import com.radiant.program.dto.ProgramResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.NotImplementedException;

public class GddsExecutionStage extends ProgramStage {
   private QnodeProgramAdapter gddsProgram;

   public GddsExecutionStage(QnodeProgramAdapter gddsProgram) {
      this.gddsProgram = gddsProgram;
   }

   void handleRequest(ProgramRequest request, ProgramResponse response, HttpServletRequest originalRequest) {
      try {
         this.gddsProgram.qnodeExecute(request, response);
      } catch (NotImplementedException var5) {
         throw new GddsExecuteNotImplemented("Method qnodeExecute not implemented for program class " + this.gddsProgram.getClass().getSimpleName());
      }
   }
}
