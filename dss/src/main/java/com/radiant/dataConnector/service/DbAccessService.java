package com.radiant.dataConnector.service;

import com.radiant.dataConnector.domain.JdbcDataConnector;
import org.json.JSONArray;
import org.json.JSONObject;

public interface DbAccessService {
   boolean testConnection(JdbcDataConnector connector);

   String executeQueryForSingleString(JdbcDataConnector connector, String query);

   JSONArray executeQuery(JdbcDataConnector connector, String query);

   JSONObject executeQuery(JdbcDataConnector connector, String query, Integer pageSize, Integer askedPage);

   String getVersion(JdbcDataConnector connector);
}
