package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.handling.InternalMessageHandler;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class UniversalMessageListener {

    private final InternalMessageHandler internalMessageHandler;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Object handleMessage(JsonNormalMessage message) throws Exception {
        Object result = executor.submit(() -> internalMessageHandler.handleMessage(message)).get(5, TimeUnit.SECONDS);

        if (result != null && !(result instanceof JsonNormalMessage)) {
            var ans = new JsonNormalMessage();
            if (result.getClass().equals(Object.class)) {
                log.debug("Object cannot be serialized,set empty props as payloadMap");
                ans.setPayloadMap(Map.of());
            } else {
                ans.setPayloadMap(objectMapper.convertValue(result, Map.class));
                log.debug("Answer recognized as map-convertable, set parameter-map as payloadMap");
            }
            return ans;
        }
        return result;
    }
}
