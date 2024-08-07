package com.ravity.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class AssignAndSeekDemo {

    static Logger logger = LoggerFactory.getLogger(AssignAndSeekDemo.class);

    public static void main(String[] args) {
        //System.out.println("Hello World");

        // Create Properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        //perties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "my-third-application");
        // earliest/latest/none
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Create Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Subscribe consumer to our topics
        // consumer.subscribe(Arrays.asList("first_topic"));

        // Assign and Seek
        TopicPartition partitionsToReadFrom = new TopicPartition("first_topic", 0);
        consumer.assign(Arrays.asList(partitionsToReadFrom));
        consumer.seek(partitionsToReadFrom, 15L);

        int noOfMsgToRead = 5;
        int noRead = 0;
        boolean keepReading = true;

        // poll for data
        while(keepReading) { // For demo only dont do this
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for(ConsumerRecord<String, String> record: records) {
                noRead += 1;
                logger.info("Key: " + record.key() + " " + " Value: " + record.value() +
                        " Partition: " + record.partition() + " Offset: " + record.offset());
                if(noRead>=noOfMsgToRead){
                    keepReading = false;
                    break;
                }
            }
        }
        logger.info("Exiting application....");
    }
}
