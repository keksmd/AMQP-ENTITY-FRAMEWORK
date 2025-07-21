package dada.tuda.framework.integration;

import dada.tuda.framework.WholeAutoConfiguration;
import dada.tuda.framework.conf.RabbitContainerConfig;
import dada.tuda.framework.conf.RedisContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { RabbitContainerConfig.class, RedisContainerConfig.class, MessagingApiImplFullContextTest.class, WholeAutoConfiguration.class })
class MessagingApiImplFullContextTest {
    @Autowired
    ApplicationContext context;

    @Test
    void testContextLoadsAndContainersStarted() {
        assertNotNull("Context must load", context);
    }
}