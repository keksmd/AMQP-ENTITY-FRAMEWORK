package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class InMemoryIdempotencyProvider implements MessageStorage {
    private final int maxEntries;
    private Map<String, NormalMessage> messages;

    @Override
    public void init() {
        messages = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, NormalMessage> eldest) {
                return size() > maxEntries;
            }
        };
    }


    @Override
    public NormalMessage getByID(String operationId) {
        return messages.get(operationId);
    }



    @Override
    public boolean isProcessedById(String id) {
        return messages.containsKey(id);
    }

    @Override
    public void storeEventAsProcessed(NormalMessage message) {
        try {
            messages.put(message.getOperationId(), message);
            log.debug("saved event processed {}", message.getOperationId());
        } catch (Exception e) {
            log.error("failed to save event processed", e);
        }
    }

    @Override
    public void clear() {
        this.messages.clear();
    }


}

