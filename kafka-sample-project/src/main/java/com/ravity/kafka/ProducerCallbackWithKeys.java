package com.ravity.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerCallbackWithKeys {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Logger logger = LoggerFactory.getLogger(ProducerCallbackWithKeys.class);

        //System.out.println("Hello World");

        // Create Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i=0; i<=10; i++) {
            String topic = "first_topic";
            String key = "Id_" + i;
            String value = "New Message: " + i;

            logger.info("-----------" + key);
            // Create producer record
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key, value);
            // Produce Message
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // Executed when record is sent successfully or exception occurs
                    if (e == null) {
                        logger.info("Received new metadata \n" +
                                "Topic: " + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error(e.getMessage());
                    }
                }
            }).get(); // Synchronous. Not to be done in production.
        }



        // Flush data
        producer.flush();
        // Flush and Close Producer
        producer.close();
        System.out.println("Message sent...");
    }
}
