package dada.tuda.framework.integration.norabbit;

import dada.tuda.framework.conf.RedisContainerConfig;
import dada.tuda.framework.springconf.WholeConfig;
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
@SpringBootTest(classes = { RedisContainerConfig.class, MessagingApiImplNoRabbitTest.class, WholeConfig.class })
class MessagingApiImplNoRabbitTest {
    @Autowired
    ApplicationContext context;

    @Test
    void testContextLoadsAndContainersStarted() {
        assertNotNull("Context must load", context);
    }
}