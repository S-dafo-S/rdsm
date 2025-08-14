package com.radiant.dataConnector.domain.dto;

import javax.validation.constraints.NotNull;

public class ExecuteQueryRequest {
   private @NotNull String sqlQuery;

   public ExecuteQueryRequest(String sqlQuery) {
      this.sqlQuery = sqlQuery;
   }

   public String getSqlQuery() {
      return this.sqlQuery;
   }

   public void setSqlQuery(final String sqlQuery) {
      this.sqlQuery = sqlQuery;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ExecuteQueryRequest)) {
         return false;
      } else {
         ExecuteQueryRequest other = (ExecuteQueryRequest)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$sqlQuery = this.getSqlQuery();
            Object other$sqlQuery = other.getSqlQuery();
            if (this$sqlQuery == null) {
               if (other$sqlQuery != null) {
                  return false;
               }
            } else if (!this$sqlQuery.equals(other$sqlQuery)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof ExecuteQueryRequest;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $sqlQuery = this.getSqlQuery();
      result = result * 59 + ($sqlQuery == null ? 43 : $sqlQuery.hashCode());
      return result;
   }

   public ExecuteQueryRequest() {
   }

   public String toString() {
      return "ExecuteQueryRequest(sqlQuery=" + this.getSqlQuery() + ")";
   }
}
