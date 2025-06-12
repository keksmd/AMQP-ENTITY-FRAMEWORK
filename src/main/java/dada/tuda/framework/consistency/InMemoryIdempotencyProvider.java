package dada.tuda.framework.consistency;

import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class InMemoryIdempotencyProvider implements MessageStorage {
    private final MessageMapper mapper;
    private final Map<String, NormalMessage> messages = new HashMap<>();

    @Override
    public NormalMessage getByID(String operationId) {
        return messages.get(operationId);
    }

    @Override
    public boolean isProcessed(NormalMessage message) {
        try {
            return messages.containsValue(message);
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
            var entity = mapper.toEntity(message);
            messages.put(message.getOperationId(), entity);
            log.debug("saved event processed {}", message.getOperationId());
        } catch (Exception e) {
            log.error("failed to save event processed", e);
        }
    }


}

