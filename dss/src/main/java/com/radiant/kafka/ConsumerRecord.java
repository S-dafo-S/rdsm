package com.radiant.kafka;

import java.io.Serializable;

public class ConsumerRecord<K, V> implements Serializable {
   private String topic;
   private int partition;
   private long offset;
   private K key;
   private V value;

   public String getTopic() {
      return this.topic;
   }

   public int getPartition() {
      return this.partition;
   }

   public long getOffset() {
      return this.offset;
   }

   public K getKey() {
      return this.key;
   }

   public V getValue() {
      return this.value;
   }

   public void setTopic(final String topic) {
      this.topic = topic;
   }

   public void setPartition(final int partition) {
      this.partition = partition;
   }

   public void setOffset(final long offset) {
      this.offset = offset;
   }

   public void setKey(final K key) {
      this.key = key;
   }

   public void setValue(final V value) {
      this.value = value;
   }
}
