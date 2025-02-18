package dada.tuda.framework.normalization;

import com.redis.testcontainers.RedisContainer;
import dada.tuda.framework.configuration.autoconfig.ConfigType;
import dada.tuda.framework.configuration.autoconfig.EnableCustomConfigs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@EnableCustomConfigs(ConfigType.ALL)
@Testcontainers

@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = {IntegrationTest.class, Config.class})
public class IntegrationTest {

    @Test
    void test() {
    }
    @Test
    void testContextLoads() {
        Assertions.assertTrue(rabbitMQContainer.isRunning());
        Assertions.assertTrue(redisContainer.isRunning());
    }
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.data.redis.host", redisContainer::getRedisHost);
        registry.add("spring.data.redis.port", redisContainer::getRedisPort);
    }

    @Container
    public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.10.7-management")
            .withEnv("RABBITMQ_ERLANG_COOKIE", "myverysecureandlongcookie")
            .withExposedPorts(5672, 15672);
    @Container
    public static RedisContainer redisContainer = new RedisContainer("redis:7.0").withExposedPorts(6379);


}




