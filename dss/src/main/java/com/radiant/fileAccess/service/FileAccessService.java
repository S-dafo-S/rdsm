package com.radiant.fileAccess.service;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ParametersAreNonnullByDefault
public interface FileAccessService {
   void read(String logicalPath, String internalPath, HttpServletRequest request, HttpServletResponse response) throws Exception;

   List<String> merge(String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit) throws Exception;
}
