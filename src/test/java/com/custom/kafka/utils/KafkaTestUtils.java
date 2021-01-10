package com.custom.kafka.utils;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KafkaTestUtils {
    private final Logger log = LoggerFactory.getLogger(KafkaTestUtils.class);
    private final String bootstrapServers;
    private final String userName;
    private final String password;

    public KafkaTestUtils(String url,
                          String userName,
                          String password) {
        this.bootstrapServers = url;
        this.userName = userName;
        this.password = password;
    }

    public boolean topicsExists(List<String> topics) throws ExecutionException, InterruptedException {
        log.info("Going to check if topic exists, topics: {}", topics);
        AdminClient client = AdminClient.create(adminProps());
        ListTopicsResult currentTopics = client.listTopics();
        Set<String> names = currentTopics.names().get();
        return names.containsAll(topics);
    }

    public void waitForTopics(List<String> topics) throws InterruptedException {
        log.info("Going to wait until topic are created, topics: {}", topics);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps());
        TestUtils.waitForCondition(() -> {
            Set<String> names = consumer.listTopics().keySet();
            return names.containsAll(topics);
        }, 10000L, "Waited for topics to become available, but took too much time.");
    }

    public void createTopics(List<String> topics) {
        log.info("Going to create topics, topics: {}", topics);
        AdminClient client = AdminClient.create(adminProps());
        List<NewTopic> newTopics = topics.stream()
                .map(s -> new NewTopic(s, 1, (short) 1).configs(new HashMap<>()))
                .collect(Collectors.toList());
        client.createTopics(newTopics);
    }

    public Set<String> getTopics() {
        log.info("Going to get all topics names");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps());
        return consumer.listTopics().keySet();
    }

    Properties consumerProps() {
        Properties properties = adminProps();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return properties;
    }

    Properties producerProps() {
        Properties properties = adminProps();
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return properties;
    }

    Properties adminProps() {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        properties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");

        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, getJaasPlainModuleConfig(userName, password));
        return properties;
    }

    private String getJaasPlainModuleConfig(String jaasUsername, String jaasPassword) {
        return String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=%s password=%s;", jaasUsername, jaasPassword);
    }
}
