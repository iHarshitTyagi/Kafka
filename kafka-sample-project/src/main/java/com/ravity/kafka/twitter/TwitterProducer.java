package com.ravity.kafka.twitter;

import com.google.common.collect.Lists;
import com.ravity.kafka.ConsumerDemo;
import com.ravity.kafka.ConsumerRunnable;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    static Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

    public  TwitterProducer() {

    }

    public void run() {
        //System.out.println("Hello World");
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        // Create Twitter Client
        Client client = createTwitterClient(msgQueue);
        client.connect();

        // Create Kafka Producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping gracefully, closing client and producerss...");
            client.stop();
            producer.close();
        }));

        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                client.stop();
                throw new RuntimeException(e);
            }
            if(msg!=null){
                logger.info(msg);
                // Create producer record
                ProducerRecord<String, String> record = new ProducerRecord<String, String>("twitter_tweets",null, msg);
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        // Executed when record is sent successfully or exception occurs
                        if (e == null) {
                        } else {
                            logger.error(e.getMessage());
                        }
                    }
                });

            }
        }
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        /* Optional: set up some followings and track terms */
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("football");
        //hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1("seN7e4ex9EYShff3xwdEcSvCD", "Ienq2IipwBXjrKeVAhg7xGvSSMqr2pM66yRkWSNd80ByzwzSAT", "2769980509-tD663ssF5DRljmargoNUDt1914uWM5YrWR0m8rj", "uPgjO527uDPl3IAoJxCyUSc5lyVcrdApjrFxQG06eyuMY");

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        // Attempts to establish a connection.
        //hosebirdClient.connect();
        return hosebirdClient;
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        // Create Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create a Safe Producer (Uncomment if required)
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // kAFKA 2.0

        // High Throughput Settings (expense of a bit of latency and CPU usage)
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024)); // 32 KB (default 16 KB)

        // Create Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        return producer;
    }

    public static void main(String[] args) {
        // Write Tweets
        new TwitterProducer().run();

    }
}
