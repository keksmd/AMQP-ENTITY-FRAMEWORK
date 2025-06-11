package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.handling.InternalMessageHandler;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class UniversalMessageListener {

    private final InternalMessageHandler internalMessageHandler;
    private final ObjectMapper objectMapper;

    public Object handleMessage(JsonNormalMessage message, Message raw) throws Exception {
        Object result = internalMessageHandler.handleMessage(message, raw);
        if (result != null && !(result instanceof JsonNormalMessage)) {
            var ans = new JsonNormalMessage();
            ans.setPayloadMap(objectMapper.convertValue(result, Map.class));
            return ans;
        }
        return result;
    }
}
