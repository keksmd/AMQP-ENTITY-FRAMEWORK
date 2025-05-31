package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class UniversalMessageListener implements MessageListener {

    private final MessageHandlerRegistry messageHandlerRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message) {
        String raw = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            NormalMessage normalMessage = objectMapper.readValue(raw, JsonNormalMessage.class);
            messageHandlerRegistry.handleMessage(normalMessage);
        } catch (Exception e) {
            log.error("Failed to handle message {}\n{}", raw, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
