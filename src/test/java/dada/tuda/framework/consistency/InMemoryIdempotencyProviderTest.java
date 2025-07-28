package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.junit.jupiter.api.BeforeEach;

class InMemoryIdempotencyProviderTest extends AbstractMessageStorageTest {

    private InMemoryIdempotencyProvider provider;

    @BeforeEach
    void setup() {
        provider = new InMemoryIdempotencyProvider(10);
        provider.init();
    }

    @Override
    protected MessageStorage getMessageStorage() {
        return provider;
    }

    @Override
    protected NormalMessage sampleMessage() {
        NormalMessage message = new JsonNormalMessage();
        message.setOperationId("test-id");
        return message;
    }


}