package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class UniversalMessageListener {

    private final MessageHandlerRegistry messageHandlerRegistry;
    private final ObjectMapper objectMapper;

    public Object handleMessage(JsonNormalMessage message) throws Exception {
        Object result = messageHandlerRegistry.handleMessage(message);
        if (result != null && !(result instanceof JsonNormalMessage)) {
            var ans = new JsonNormalMessage();
            ans.setPayloadMap(objectMapper.convertValue(result, Map.class));
            return ans;
        }
        return result;
    }
}
