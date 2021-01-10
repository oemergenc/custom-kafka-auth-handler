package com.custom.kafka.integration;

import com.custom.kafka.utils.KafkaTestUtils;
import org.apache.kafka.common.errors.SaslAuthenticationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class AuthTestIT {
    public static String allowedUsername = "test";
    public static String allowedUserpassword = "testpw";

    public static String bootstrapServers;
    public static DockerComposeContainer kafkaContainer = new DockerComposeContainer(new File("docker-compose.yml"))
            .withExposedService("kafka", 9093);

    @BeforeClass
    public static void beforeAll() {
        kafkaContainer.start();
        bootstrapServers = String.format("%s:%s", kafkaContainer.getServiceHost("kafka", 9092), 9092);
    }

    @AfterClass
    public static void afterAll() {
        kafkaContainer.stop();
    }

    @Test
    public void shouldSucceedCreatingTopics() throws InterruptedException {
        //given
        List<String> topicsToCreate = Arrays.asList("customerTopic", "ordersTopic");
        KafkaTestUtils kafkaTestUtils = new KafkaTestUtils(bootstrapServers, allowedUsername, allowedUserpassword);

        //when
        kafkaTestUtils.createTopics(topicsToCreate);
        kafkaTestUtils.waitForTopics(topicsToCreate);

        //then
        Set<String> topics = kafkaTestUtils.getTopics();
        assertTrue(topics.containsAll(topicsToCreate));
    }

    @Test(expected = SaslAuthenticationException.class)
    public void shouldFailCreatingTopicsUsingWrongUser() {
        //given
        String unknownUser = "anUnknownUser";
        List<String> topicsToCreate = Arrays.asList("aaa-customerTopic", "aaa-ordersTopic");
        KafkaTestUtils kafkaTestUtils = new KafkaTestUtils(bootstrapServers, unknownUser, allowedUserpassword);

        //when
        kafkaTestUtils.createTopics(topicsToCreate);
        kafkaTestUtils.getTopics();
    }

    @Test(expected = SaslAuthenticationException.class)
    public void shouldFailCreatingTopicsUsingWrongPassword() {
        //given
        String wrongUserPassword = "aWrongPassword";
        List<String> topicsToCreate = Arrays.asList("bbb-customerTopic", "bbb-ordersTopic");
        KafkaTestUtils kafkaTestUtils = new KafkaTestUtils(bootstrapServers, allowedUsername, wrongUserPassword);

        //when
        kafkaTestUtils.createTopics(topicsToCreate);
        kafkaTestUtils.getTopics();
    }
}
