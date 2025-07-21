package dada.tuda.framework.integration.norabbitnoredis;

import dada.tuda.framework.WholeAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j

@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { NoRedisNoRabbitContextTest.class, WholeAutoConfiguration.class })
class NoRedisNoRabbitContextTest {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    void testContextLoadsAndContainersStarted() {
        assertNotNull(applicationContext);
    }
}