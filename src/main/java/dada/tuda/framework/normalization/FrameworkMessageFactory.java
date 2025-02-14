package dada.tuda.framework.normalization;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FrameworkMessageFactory {
    private final ObjectMapper objectMapper;

    public AbstractNormalMessage createAbstract(@Nullable String objectId, @Nullable @Valid Object payload, IMessagingEventType type) {
        Map<String, Object> payloadMap = payload == null ? Collections.emptyMap() : objectMapper.convertValue(payload, Map.class);
        return new AbstractNormalMessage(objectId, payloadMap, type);
    }

    public AbstractNormalMessage createAbstractWithActor(@Nullable String objectId,
                                                         @Nullable String actorId, @Nullable @Valid Object payload, IMessagingEventType type) {
        Map<String, Object> payloadMap = payload == null ? Collections.emptyMap() : objectMapper.convertValue(payload, Map.class);
        return new AbstractNormalMessage(objectId, actorId, payloadMap, type);
    }
    public AbstractNormalMessage canceled(String operationId, String reason, IMessagingEventType type) {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("reason", reason);
        return new AbstractNormalMessage(operationId, payloadMap, type);

    }

}
