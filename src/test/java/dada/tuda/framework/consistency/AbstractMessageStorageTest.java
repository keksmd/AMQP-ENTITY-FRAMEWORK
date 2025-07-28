package dada.tuda.framework.consistency;

import dada.tuda.framework.crud.MessageJPAEntity;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractMessageStorageTest {
    @Test
    void testSerializationAndDeserializationPreserveMessageFields() {
        MessageStorage storage = getMessageStorage();
        NormalMessage original = new JsonNormalMessage(
                "obj-123",
                "actor-456",
                Map.of("key1", "value1", "num", 42),
                "domainA",
                "create"
        );
        original.setOperationId(UUID.randomUUID().toString());
        // given
        MessageJPAEntity entity = new MessageJPAEntity();
        entity.setObjectId(original.getObjectId());
        entity.setActorId(original.getActorId());
        entity.setDomainName(original.getDomainName());
        entity.setActionTypeName(original.getActionTypeName());
        entity.setOperationId(original.getOperationId());
        entity.setPayloadMap(original.getPayloadMap());
        entity.setTtl(10_000L);

        storage.storeEventAsProcessed(original);

        NormalMessage fromRedis = storage.getByID(original.getOperationId());

        Assertions.assertNotNull(fromRedis);
        assertEquals(original.getOperationId(), fromRedis.getOperationId());
        assertEquals(original.getObjectId(), fromRedis.getObjectId());
        assertEquals(original.getActorId(), fromRedis.getActorId());
        assertEquals(original.getActionTypeName(), fromRedis.getActionTypeName());
        assertEquals(original.getDomainName(), fromRedis.getDomainName());
        assertEquals(original.getPayloadMap(), fromRedis.getPayloadMap());
    }

    protected abstract MessageStorage getMessageStorage();

    @Test
    void testStoreAndGetById() {
        MessageStorage storage = getMessageStorage();
        NormalMessage message = sampleMessage();

        storage.storeEventAsProcessed(message);

        assertNotNull(storage.getByID(message.getOperationId()));
    }

    protected abstract NormalMessage sampleMessage();

    @Test
    void testOverwritingDifferentMessageClassesWithSameId() {
        MessageStorage provider = getMessageStorage();
        String commonOperationId = "test-id";
        JsonNormalMessage message = new JsonNormalMessage(
                "obj-A",
                "actor-A",
                Map.of("type", "A"),
                "domain",
                "action"
        );
        message.setOperationId(commonOperationId);


        JsonNormalMessage messageA = new JsonNormalMessage(
                "obj-A",
                "actor-A",
                Map.of("type", "A"),
                "domain",
                "action"
        );
        messageA.setOperationId(commonOperationId);

        MessageJPAEntity messageB = new MessageJPAEntity();
        messageB.setObjectId("obj-B");
        messageB.setActorId("actor-B");
        messageB.setDomainName("domain");
        messageB.setActionTypeName("action");
        messageB.setPayloadMap(Map.of("type", "B"));
        messageB.setOperationId(commonOperationId); // одинаковый ID с messageA

        // When
        provider.storeEventAsProcessed(messageA);
        NormalMessage retrieved = provider.getByID(commonOperationId);
        assertNotNull(retrieved);
        assertEquals(messageA.getOperationId(), provider.getByID(commonOperationId).getOperationId());
        assertTrue(provider.isProcessed(messageB));


        provider.storeEventAsProcessed(messageB);
        retrieved = provider.getByID(commonOperationId);
        assertTrue(provider.isProcessed(messageA));
        assertTrue(provider.isProcessed(messageB));

        // Then
        assertNotNull(retrieved);
        assertNotEquals(messageA, retrieved); // перезаписан
        assertEquals(messageB.getObjectId(), retrieved.getObjectId());
        assertEquals("obj-B", retrieved.getObjectId());
        assertEquals("actor-B", retrieved.getActorId());
        assertEquals(Map.of("type", "B"), retrieved.getPayloadMap());
    }

    @Test
    void testIsProcessedById() {
        MessageStorage storage = getMessageStorage();
        NormalMessage message = sampleMessage();

        assertFalse(storage.isProcessedById(message.getOperationId()));

        storage.storeEventAsProcessed(message);

        assertTrue(storage.isProcessedById(message.getOperationId()));
    }

    @Test
    void testIsProcessedByMessage() {
        MessageStorage storage = getMessageStorage();
        NormalMessage message = sampleMessage();

        assertFalse(storage.isProcessed(message));

        storage.storeEventAsProcessed(message);

        assertTrue(storage.isProcessed(message));
    }
}