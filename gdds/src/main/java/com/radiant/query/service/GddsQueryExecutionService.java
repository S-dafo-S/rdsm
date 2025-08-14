package com.radiant.query.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface GddsQueryExecutionService {
   String execute(Long id, String queryName, Map<String, String> params, HttpServletRequest request, Boolean isDssId);
}
