package dada.tuda.framework.normalization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.EntityContext;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Map;

@RequiredArgsConstructor
public class PayloadConverter {
    private final EntityContext entityContext;
    private final ObjectMapper objectMapper;

    public <T> T convertPayload(Map<String, Object> payloadMap, Class<T> payloadType) {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        var descriptor = entityContext.getDescriptorByMessagingEntityClass(payloadType);
        T payload = objectMapper.convertValue(payloadMap, payloadType);
        if (descriptor != null) {
            String payloadName = descriptor.getPayload();
            try {
                Field payloadFiled = payloadType.getDeclaredField(payloadName);
                payloadFiled.setAccessible(true);
                payloadMap.remove(descriptor.getActorIdField());
                payloadMap.remove(descriptor.getObjectIdFiled());
                payloadMap.remove(descriptor.getOperationIdFiled());
                payloadFiled.set(payload, payloadMap);
            } catch (NoSuchFieldException | IllegalAccessException e) {

            }

        }

        return payload;
    }
}
