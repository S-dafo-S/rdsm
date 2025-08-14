package com.radiant.dataConnector.service;

import com.radiant.account.service.UpdateEntityHelper;
import com.radiant.dataConnector.domain.DbmsType;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

public final class DbAccessUtil {
   public static String getJdbcUrl(JdbcDataConnector connector) {
      return StringUtils.isNotEmpty(connector.getCustomJdbcUrl()) ? connector.getCustomJdbcUrl() : getJdbcUrl(connector.getDbmsType(), connector.getHostname(), connector.getPort(), connector.getDbName());
   }

   public static String getJdbcUrl(DbmsType dbType, String hostName, Integer port, String dbName) {
      Map<String, Object> values = new HashMap();
      values.put("hostname", hostName);
      if (port == null) {
         port = dbType.getDefaultPort();
      }

      values.put("port", port);
      values.put("dbName", dbName);
      StringSubstitutor substitutor = new StringSubstitutor(values);
      return substitutor.replace(dbType.getJdbcUrlFormat());
   }

   static Properties getJdbcConnectionProperties(JdbcDataConnector connector) {
      Properties properties = new Properties();
      UpdateEntityHelper.ifNotNull(connector.getUserId(), (userId) -> properties.setProperty("user", userId));
      UpdateEntityHelper.ifNotNull(connector.getUserPassword(), (password) -> properties.setProperty("password", password));
      return properties;
   }
}
