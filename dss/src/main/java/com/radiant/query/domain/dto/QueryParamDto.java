package com.radiant.query.domain.dto;

import java.util.Map;

public class QueryParamDto {
   private String key;
   private String value;

   public QueryParamDto(Map.Entry<String, String> entry) {
      this.key = (String)entry.getKey();
      this.value = (String)entry.getValue();
   }

   public String getKey() {
      return this.key;
   }

   public String getValue() {
      return this.value;
   }

   public void setKey(final String key) {
      this.key = key;
   }

   public void setValue(final String value) {
      this.value = value;
   }

   public QueryParamDto() {
   }
}
