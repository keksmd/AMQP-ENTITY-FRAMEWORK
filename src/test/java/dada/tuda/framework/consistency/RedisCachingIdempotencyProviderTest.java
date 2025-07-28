package dada.tuda.framework.consistency;

import dada.tuda.framework.WholeAutoConfiguration;
import dada.tuda.framework.conf.RedisContainerConfig;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;


@Slf4j
@Testcontainers
@ActiveProfiles("test")
@ImportAutoConfiguration(RedisAutoConfiguration.class)
@SpringBootTest(classes = { WholeAutoConfiguration.class, RedisContainerConfig.class }, properties =
        "dada.tuda.framework.messaging.saga.enabled=true")
@TestPropertySource(locations = "classpath:application.yml")
class RedisCachingIdempotencyProviderTest extends AbstractMessageStorageTest {
    @Autowired
    private RedisCachingIdempotencyProvider storage;

    @AfterEach
    void clear() {
        storage.clear();
    }

    @Override
    protected MessageStorage getMessageStorage() {
        return storage;
    }

    @Override
    protected NormalMessage sampleMessage() {
        NormalMessage message = new JsonNormalMessage();
        message.setOperationId("test-id");
        return message;
    }
}