package com.radiant.dataConnector.service;

import com.radiant.dataConnector.domain.DataConnectorType;
import com.radiant.dataConnector.domain.DbmsType;
import com.radiant.dataConnector.domain.JdbcDataConnector;
import com.radiant.exception.jdbc.DbDriverNotFoundException;
import com.radiant.exception.jdbc.JdbcException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@ParametersAreNonnullByDefault
public class DbAccessServiceImpl implements DbAccessService {
   private static final Logger LOG = LoggerFactory.getLogger(DbAccessServiceImpl.class);
   private static final int CONNECTION_TIMEOUT_SECONDS = 10;
   private final Map<DataConnectorType, Driver> driverInstances = new HashMap<>();
   @Value("${app.jdbc.drivers.dir}")
   private String driverDirectories;

   public boolean testConnection(JdbcDataConnector connector) {
      try (Connection connection = this.getConnection(connector)) {
         return connection.getMetaData() != null;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public String executeQueryForSingleString(JdbcDataConnector connector, String query) throws JdbcException {
      try (Connection connection = this.getConnection(connector);
           Statement stmt = connection.createStatement();
           ResultSet resultSet = stmt.executeQuery(query)) {
         resultSet.next();
         return resultSet.getString(1);
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public JSONArray executeQuery(JdbcDataConnector connector, String query) {
      LOG.info("Executing query: {}", query);
      try (Connection connection = this.getConnection(connector);
           Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
           ResultSet resultSet = stmt.executeQuery(query)) {
         JSONArray jsonResult = this.readResultSet(resultSet);
         LOG.info("Query result: {}", jsonResult.toString(4));
         return jsonResult;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public JSONObject executeQuery(JdbcDataConnector connector, String query, Integer pageSize, Integer pageNum) {
      LOG.info("Executing query: {}", query);
      try (Connection connection = this.getConnection(connector);
           Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
           ResultSet resultSet = stmt.executeQuery(query)) {
         resultSet.last();
         int rowsCount = resultSet.getRow();
         resultSet.beforeFirst();
         CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
         crs.setPageSize(pageSize);
         crs.populate(resultSet, pageSize * (pageNum - 1) + 1);
         JSONObject pagingWrapping = new JSONObject();
         pagingWrapping.put("page", pageNum);
         pagingWrapping.put("pageSize", pageSize);
         pagingWrapping.put("total", rowsCount);
         pagingWrapping.put("data", this.readResultSet(crs));
         LOG.info("Paged query result: {}", pagingWrapping.toString(4));
         return pagingWrapping;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public String getVersion(JdbcDataConnector connector) {
      try (Connection connection = this.getConnection(connector)) {
         DatabaseMetaData metadata = connection.getMetaData();
         return metadata.getDatabaseProductName() + " " + metadata.getDatabaseProductVersion();
      } catch (SQLException e) {
         return null;
      }
   }

   private Driver getDriverFor(JdbcDataConnector connector) {
      synchronized(this.driverInstances) {
         if (this.driverInstances.containsKey(connector.getType())) {
            Driver driver = (Driver)this.driverInstances.get(connector.getType());
            LOG.info("Using {} to connect to {}", driver, connector);
            return driver;
         } else {
            DbmsType dbmsType = connector.getDbmsType();
            Class<?> driverClass;
            if (dbmsType.getDriverCP() == null) {
               LOG.info("Looking for preloaded driver {}", dbmsType.getDriverClass());

               try {
                  driverClass = Class.forName(dbmsType.getDriverClass());
               } catch (ClassNotFoundException e) {
                  throw new DbDriverNotFoundException(connector.getHostname(), connector.getDbName(), e);
               }
            } else {
               List<URL> classPath = new ArrayList();
               String[] directories = this.driverDirectories.split(":");
               String[] driverJars = connector.getDbmsType().getDriverCP().split(":");

               for(String directory : directories) {
                  for(String driverJar : driverJars) {
                     File file = new File(directory, driverJar);
                     if (file.exists()) {
                        try {
                           classPath.add(file.toURI().toURL());
                        } catch (MalformedURLException var21) {
                           LOG.error("Bad JDBC driver file {}", file);
                        }
                     }
                  }
               }

               URL[] urls = (URL[])classPath.toArray(new URL[0]);
               LOG.info("Looking for {} in {}", dbmsType.getDriverClass(), Arrays.toString(urls));
               URLClassLoader driverClassLoader = (URLClassLoader)AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls, this.getClass().getClassLoader()));

               try {
                  driverClass = driverClassLoader.loadClass(dbmsType.getDriverClass());
               } catch (ClassNotFoundException e) {
                  LOG.error("Cannot find {} in {}", dbmsType.getDriverClass(), urls);
                  throw new DbDriverNotFoundException(connector.getHostname(), connector.getDbName(), e);
               }
            }

            Driver driver;
            try {
               driver = (Driver)driverClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
               throw new RuntimeException(e);
            }

            this.driverInstances.put(dbmsType.getType(), driver);
            LOG.info("Using {} to connect to {}", driver, connector);
            return driver;
         }
      }
   }

   private Connection getConnection(JdbcDataConnector connector) throws SQLException {
      Driver driver = this.getDriverFor(connector);
      return driver.connect(DbAccessUtil.getJdbcUrl(connector), DbAccessUtil.getJdbcConnectionProperties(connector));
   }

   private JSONArray readResultSet(ResultSet rs) throws SQLException {
      LOG.info("Query result column size: {}", rs.getMetaData().getColumnCount());
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      JSONArray jsonResult = new JSONArray();

      while(rs.next()) {
         JSONObject rowObject = new JSONObject();

         for(int i = 1; i <= columnCount; ++i) {
            rowObject.put(metaData.getColumnLabel(i), rs.getObject(i) != null ? rs.getObject(i) : "null");
         }

         jsonResult.put(rowObject);
      }

      return jsonResult;
   }
}
