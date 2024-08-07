package com.ravity.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {

    public static void main(String[] args) {
        //System.out.println("Hello World");

        // Create Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        // Create producer record
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic","Another Hello Java World");

        // Create Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Produce Message
        producer.send(record);
        // Flush data
        producer.flush();
        // Flush and Close Producer
        producer.close();
        System.out.println("Message sent...");
    }
}
