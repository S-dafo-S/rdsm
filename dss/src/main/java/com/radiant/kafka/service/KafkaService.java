package com.radiant.kafka.service;

public interface KafkaService {
   <T> void sendMessage(String topic, T msg);

   void creatListeners();
}
