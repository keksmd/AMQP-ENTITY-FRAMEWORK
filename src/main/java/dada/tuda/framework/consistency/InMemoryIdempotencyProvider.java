package dada.tuda.framework.consistency;

import dada.tuda.framework.crud.MessageJPAEntity;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class InMemoryIdempotencyProvider implements MessageStorage {
    private final Map<String, NormalMessage> messages = new HashMap<>();
    @Value("${spring.cache.redis.time-to-live:300}")
    private Long messageTtl;

    @Override
    public NormalMessage getByID(String operationId) {
        return messages.get(operationId);
    }

    @Override
    public boolean isProcessed(NormalMessage message) {
        try {
            return messages.containsKey(message.getOperationId());
        } catch (Exception e) {
            log.error("failed to check event processed {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isProcessedById(String id) {
        return messages.containsKey(id);
    }

    @Override
    public void storeEventAsProcessed(NormalMessage message) {
        try {
            var entity = new MessageJPAEntity(message);
            entity.setTtl(messageTtl);
            messages.put(message.getOperationId(), entity);
            log.debug("saved event processed {}", message.getOperationId());
        } catch (Exception e) {
            log.error("failed to save event processed", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

