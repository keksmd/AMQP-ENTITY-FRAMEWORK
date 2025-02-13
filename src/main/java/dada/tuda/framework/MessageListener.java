package dada.tuda.framework;

import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.types.realizations.CancellingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
@RequiredArgsConstructor
public class MessageListener {
    private final MessageHandlerRegistry handlerRegistry;

    @RabbitHandler
    public Object handleEventCreatedEvent(@Payload AbstractNormalMessage event) throws Exception {
        if (event.computeType() instanceof CancellingEvent) {
            String reason = (String) event.getPayloadMap().get("reason");
            if (reason != null) {
                log.warn("operation {} id cancelling because {}", event.getOperationId(), reason);
            }
            handlerRegistry.cancelMessage(event.getOperationId());
        }
        return handlerRegistry.handleMessage(event);
    }
}

