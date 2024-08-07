package com.ravity.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

// Threaded consumer
public class ConsumerRunnable implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

    //System.out.println("Hello World");
    String bootstrap = "127.0.0.1:9092";
    String consumerGroup = "my-eigth-application";
    String topic = "first_topic";
    String autoOffset = "earliest";

    private CountDownLatch latch; // For Shutdown
    private KafkaConsumer<String, String> consumer;

    public ConsumerRunnable(CountDownLatch latch) {
        this.latch = latch;
        // Create Consumer
        consumer = new KafkaConsumer<>(getProperties());
        // Subscribe consumer to our topics
        consumer.subscribe(Arrays.asList(topic));
    }

    @Override
    public void run() {
        try {
            // poll for data
            while (true) { // For demo only dont do this
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Key: " + record.key() + " " + " Value: " + record.value() +
                            " Partition: " + record.partition() + " Offset: " + record.offset());
                }
            }
        } catch (WakeupException e) {
            logger.error("Received shutdown exception....");
        } finally {
            consumer.close();
            // Inform Main thread consumer is done consuming
            latch.countDown();
        }

    }

    public Properties getProperties() {
        // Create Properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        // earliest/latest/none
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }

    public void shutdown() {
        // Interrupt consumer polling
        // Throws WakeupException
        consumer.wakeup();
    }
}
