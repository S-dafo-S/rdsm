package com.radiant.query.util;

public interface DbCredentials {
   String getDbType();

   String getHostname();

   int getPort();

   String getUserId();

   String getUserPassword();

   String getDbname();
}
