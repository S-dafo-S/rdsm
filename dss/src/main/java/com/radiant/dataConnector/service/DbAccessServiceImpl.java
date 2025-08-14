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
      try {
         Connection connection = this.getConnection(connector);
         Throwable var3 = null;

         boolean var4;
         try {
            var4 = connection.getMetaData() != null;
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (connection != null) {
               if (var3 != null) {
                  try {
                     connection.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  connection.close();
               }
            }

         }

         return var4;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public String executeQueryForSingleString(JdbcDataConnector connector, String query) throws JdbcException {
      try {
         Connection con = this.getConnection(connector);
         Throwable var4 = null;

         Object var9;
         try {
            Statement stmt = con.createStatement();
            Throwable var6 = null;

            try {
               ResultSet rs = stmt.executeQuery(query);
               Throwable var8 = null;

               try {
                  rs.next();
                  var9 = rs.getString(1);
               } catch (Throwable var56) {
                  var9 = var56;
                  var8 = var56;
                  throw var56;
               } finally {
                  if (rs != null) {
                     if (var8 != null) {
                        try {
                           rs.close();
                        } catch (Throwable var55) {
                           var8.addSuppressed(var55);
                        }
                     } else {
                        rs.close();
                     }
                  }

               }
            } catch (Throwable var58) {
               var6 = var58;
               throw var58;
            } finally {
               if (stmt != null) {
                  if (var6 != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var54) {
                        var6.addSuppressed(var54);
                     }
                  } else {
                     stmt.close();
                  }
               }

            }
         } catch (Throwable var60) {
            var4 = var60;
            throw var60;
         } finally {
            if (con != null) {
               if (var4 != null) {
                  try {
                     con.close();
                  } catch (Throwable var53) {
                     var4.addSuppressed(var53);
                  }
               } else {
                  con.close();
               }
            }

         }

         return (String)var9;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public JSONArray executeQuery(JdbcDataConnector connector, String query) {
      LOG.info("Executing query: {}", query);

      try {
         Connection con = this.getConnection(connector);
         Throwable var4 = null;

         JSONArray var10;
         try {
            Statement stmt = con.createStatement(1004, 1007);
            Throwable var6 = null;

            try {
               ResultSet rs = stmt.executeQuery(query);
               Throwable var8 = null;

               try {
                  JSONArray jsonResult = this.readResultSet(rs);
                  LOG.info("Query result: {}", jsonResult.toString(4));
                  var10 = jsonResult;
               } catch (Throwable var57) {
                  var8 = var57;
                  throw var57;
               } finally {
                  if (rs != null) {
                     if (var8 != null) {
                        try {
                           rs.close();
                        } catch (Throwable var56) {
                           var8.addSuppressed(var56);
                        }
                     } else {
                        rs.close();
                     }
                  }

               }
            } catch (Throwable var59) {
               var6 = var59;
               throw var59;
            } finally {
               if (stmt != null) {
                  if (var6 != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var55) {
                        var6.addSuppressed(var55);
                     }
                  } else {
                     stmt.close();
                  }
               }

            }
         } catch (Throwable var61) {
            var4 = var61;
            throw var61;
         } finally {
            if (con != null) {
               if (var4 != null) {
                  try {
                     con.close();
                  } catch (Throwable var54) {
                     var4.addSuppressed(var54);
                  }
               } else {
                  con.close();
               }
            }

         }

         return var10;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public JSONObject executeQuery(JdbcDataConnector connector, String query, Integer pageSize, Integer pageNum) {
      LOG.info("Executing query: {}", query);

      try {
         Connection con = this.getConnection(connector);
         Throwable var6 = null;

         JSONObject var14;
         try {
            Statement stmt = con.createStatement(1004, 1007);
            Throwable var8 = null;

            try {
               ResultSet rs = stmt.executeQuery(query);
               Throwable var10 = null;

               try {
                  rs.last();
                  int rowsCount = rs.getRow();
                  rs.beforeFirst();
                  CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                  crs.setPageSize(pageSize);
                  crs.populate(rs, pageSize * (pageNum - 1) + 1);
                  JSONObject pagingWrapping = new JSONObject();
                  pagingWrapping.put("page", pageNum);
                  pagingWrapping.put("pageSize", pageSize);
                  pagingWrapping.put("total", rowsCount);
                  pagingWrapping.put("data", this.readResultSet(crs));
                  LOG.info("Paged query result: {}", pagingWrapping.toString(4));
                  var14 = pagingWrapping;
               } catch (Throwable var61) {
                  var10 = var61;
                  throw var61;
               } finally {
                  if (rs != null) {
                     if (var10 != null) {
                        try {
                           rs.close();
                        } catch (Throwable var60) {
                           var10.addSuppressed(var60);
                        }
                     } else {
                        rs.close();
                     }
                  }

               }
            } catch (Throwable var63) {
               var8 = var63;
               throw var63;
            } finally {
               if (stmt != null) {
                  if (var8 != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var59) {
                        var8.addSuppressed(var59);
                     }
                  } else {
                     stmt.close();
                  }
               }

            }
         } catch (Throwable var65) {
            var6 = var65;
            throw var65;
         } finally {
            if (con != null) {
               if (var6 != null) {
                  try {
                     con.close();
                  } catch (Throwable var58) {
                     var6.addSuppressed(var58);
                  }
               } else {
                  con.close();
               }
            }

         }

         return var14;
      } catch (SQLException e) {
         throw new JdbcException(connector.getHostname(), connector.getDbName(), connector.getDbVersion(), connector.getCustomJdbcUrl(), e);
      }
   }

   public String getVersion(JdbcDataConnector connector) {
      try {
         Connection connection = this.getConnection(connector);
         Throwable var3 = null;

         String var5;
         try {
            DatabaseMetaData meta = connection.getMetaData();
            var5 = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
         } catch (Throwable var15) {
            var3 = var15;
            throw var15;
         } finally {
            if (connection != null) {
               if (var3 != null) {
                  try {
                     connection.close();
                  } catch (Throwable var14) {
                     var3.addSuppressed(var14);
                  }
               } else {
                  connection.close();
               }
            }

         }

         return var5;
      } catch (SQLException var17) {
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
