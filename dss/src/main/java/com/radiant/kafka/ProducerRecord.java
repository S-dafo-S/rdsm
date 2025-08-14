package com.radiant.kafka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProducerRecord<V> implements Serializable {
   private List<Map<String, Object>> records = new ArrayList();

   public ProducerRecord(String key, V value) {
      Map<String, Object> map = new HashMap();
      if (key != null) {
         map.put("key", key);
      }

      if (value != null) {
         map.put("value", value);
      }

      this.records.add(map);
   }

   public ProducerRecord() {
   }

   public List<Map<String, Object>> getRecords() {
      return this.records;
   }

   public void setRecords(final List<Map<String, Object>> records) {
      this.records = records;
   }
}
