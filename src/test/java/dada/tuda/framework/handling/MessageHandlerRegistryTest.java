package dada.tuda.framework.handling;

import dada.tuda.framework.WholeConfig;
import dada.tuda.framework.conf.RabbitContainerConfig;
import dada.tuda.framework.conf.RedisContainerConfig;
import dada.tuda.framework.conf.RepoConfig;
import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.conf.beans.TestEntityRepo;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.crud.DescriptorConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { RepoConfig.class, TestEntity.class, RabbitContainerConfig.class, RedisContainerConfig.class, WholeConfig.class })

/**
 *
 * По неизвестным причинам (скорее всего кэширование тест-контектса спринга между тест-классам)
 * часть этих тестов не проходит при запуске сразу нескольких тест-классов
 * в maven test и при запуске только этого класса должны проходить
 *  TODO выяснить причину и решить пробоему
 */

class MessageHandlerRegistryTest {

    String operationId;
    @Autowired
    DescriptorConverter descriptorConverter;
    @Autowired
    MessageStorage storage;
    @Autowired
    private TestEntityRepo testRepo;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        operationId = UUID.randomUUID().toString();
        descriptorConverter.setOperationIdGenerator(() -> operationId);
    }

    @Test
    void msgSendedAndReaded() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.setField1(" new  f 1q");
        testEntity.setId("1");
        testEntity.setObject("test");
        testRepo.create(testEntity);
        Thread.sleep(5000);
        assertNotNull(storage.getByID(operationId));
    }

    @Test
    void msgSendedAndRCanceled() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.setField1(" new  f 1q");
        testEntity.setId("2");
        testEntity.setObject(null);
        testRepo.create(testEntity);
        Thread.sleep(5000);
        assertNull(storage.getByID(operationId));
    }

    @Test
    void msgDuplicatedAndSecondRetried() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.setField1(" new  f 1q");
        testEntity.setId("3");
        testEntity.setObject(null);
        testRepo.create(testEntity);
        Thread.sleep(5000);
        assertNull(storage.getByID(operationId));

        testEntity.setObject("retry");
        testRepo.create(testEntity);
        Thread.sleep(5000);
        assertNotNull(storage.getByID(operationId));
    }

    @Test
    void msgDuplicatedAndSecondCanceled() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.setField1(" new  f 1q");
        testEntity.setId("4");
        testEntity.setObject("test");

        testRepo.create(testEntity);
        Thread.sleep(5000);
        assertNotNull(storage.getByID(operationId));
        assert (storage.isProcessedById(operationId));
        testEntity.setObject("test2");
        testRepo.create(testEntity);
        Thread.sleep(5000);
        assert ("test".equals(storage.getByID(operationId).getObjectId()));
    }
}