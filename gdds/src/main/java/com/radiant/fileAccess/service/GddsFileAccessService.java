package com.radiant.fileAccess.service;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ParametersAreNonnullByDefault
public interface GddsFileAccessService {
   void read(Long courtId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest request, boolean saveToTmp);

   void readForDss(Long dssId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest request, boolean saveToTmp);

   /** @deprecated */
   @Deprecated
   void read(Long courtId, String logicalPath, String internalPath, HttpServletResponse response, HttpServletRequest request);

   /** @deprecated */
   @Deprecated
   List<String> merge(Long courtId, String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit);

   List<String> mergeForDss(Long dssId, String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit);
}
