package dada.tuda.framework.handling;

import dada.tuda.framework.consistency.EventStorager;
import dada.tuda.framework.consistency.IdempotencyProvider;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class MessageHandlerRegistry {
    private final List<MessageHandler> handlers;
    private final IdempotencyProvider idempotencyProvider;
    private final EventStorager eventStorager;
    private final Map<IMessagingEventType, MessageHandler> cache = new HashMap<>();
    private final MessageCanceller messageCanceller;
    @Value("${dada.tuda.messaging.store-only-cancelable:true}")
    private boolean storeOnlyCancellableEvents;
    @Value("${dada.tuda.messaging.saga.enabled:false}")
    private Boolean sagaEnabled;
    @Value("${spring.application.name}")
    private String serviceName;


    public Object handleMessage(AbstractNormalMessage message) throws Exception {
        if (message.getType() instanceof CancelUtils.CancellingEvent) {
            String reason = (String) message.getPayloadMap().get("reason");
            if (reason != null) {
                log.warn("operation with id={} is cancelling. \nReason: {}", message.getOperationId(), reason);
            }
            this.cancelMessage(message.getOperationId());
        }

        MessageHandler cachedHandler = getHandlerByType(message.getType());
        if (!idempotencyProvider.eventProcessed(message.getOperationId())) {
            try {
                Object returned = cachedHandler.handle(message);
                if (!storeOnlyCancellableEvents || cachedHandler instanceof CancelableMessageHandler && eventStorager.isEnabled()) {
                    eventStorager.save(message);
                }
                idempotencyProvider.storeEventAsProcessed(message.getOperationId());
                return returned;
            } catch (Exception e) {
                log.warn("operation {} should be canceled: \n {}", message.getOperationId(), e.getMessage());
                if (Boolean.TRUE.equals(sagaEnabled)) {
                    String id = messageCanceller.cancelOperation(message.getOperationId(), "Exception in service: " + serviceName + " " + e.getLocalizedMessage(), message.getType());
                    idempotencyProvider.storeEventAsProcessed(id);
                    log.warn(e.getLocalizedMessage());
                    return null;
                } else {
                    throw e;
                }
            }
        } else {
            log.warn("operation already processed: {}", message.getOperationId());
            return null;
        }
    }

    public void cancelMessage(String eventId) throws Exception {
        AbstractNormalMessage canceledEvent = eventStorager.getByID(eventId);
        if (canceledEvent == null) {
            log.error("Operation with id {} cannot be cancelled because it is not stored before.", eventId);
            return;
        }
        var cachedHandler = getHandlerByType(canceledEvent.getType());
        if (cachedHandler instanceof CancelableMessageHandler cancelableMessageHandler) {
            cancelableMessageHandler.cancel(canceledEvent);
        }
    }
    private MessageHandler getHandlerByType(IMessagingEventType type) {
        return cache
                .computeIfAbsent(type, key
                                -> handlers
                                .stream()
                                .filter(handler ->
                                        handler.canHandle(key))
                        .findFirst().
                        orElse(defaultHandler));
    }
    private final MessageHandler defaultHandler = new MessageHandler() {
        @Override
        public Boolean canHandle(IMessagingEventType type) {
            return true;
        }

        @Override
        public Object handle(AbstractNormalMessage message) {
            log.warn("Message with id has no valid handler: {}",message.toString());
            return null;
        }
    };


}
