package dada.tuda.framework.handling;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class MessageHandlerRegistry {
    private final MessageStorage messageStorage;
    private final Map<Pair<IMessagingDomain, IEventAction>, MessageHandler> cache = new ConcurrentHashMap<>();
    private final MessageCanceller messageCanceller;
    private final MessageMapper mapper;
    private final List<MessageHandler> handlers;
    private final ObjectProvider<DadaTudaFrameworkProperties> provider;
    private DadaTudaFrameworkProperties properties;
    @Value("${spring.application.name}")
    private String serviceName;

    @PostConstruct
    void init() {
        properties = provider.getIfAvailable();
    }

    public Object handleMessage(NormalMessage message) throws Exception {
        return this.handleIntenal(mapper.normalize(message));
    }

    public Object handleIntenal(NormalizedMessage message) throws Exception {
        if (message.getActionType().isCancel()) {
            String reason = (String) message.getPayloadMap().get("reason");
            if (reason != null) {
                log.warn("operation with id={} is cancelling. \nReason: {}", message.getOperationId(), reason);
            }
            this.cancelMessage(message);
            return null;
        }

        MessageHandler cachedHandler = getHandler(message);

        if (!messageStorage.isProcessed(message)) {
            try {
                Object returned = cachedHandler.handle(message);
                if ((!properties.getMessaging().isStoreOnlyCancelable() || cachedHandler instanceof CancelableMessageHandler) && messageStorage.isEnabled()) {
                    messageStorage.storeEventAsProcessed(message);
                }
                if (message.getActionType().isQuery()) {
                    return returned;
                } else {
                    return null;
                }

            } catch (Exception e) {
                log.warn("operation {} should be canceled: \n {}", message.getOperationId(), e.getMessage());
                if (Boolean.TRUE.equals(properties.getMessaging().getSaga().isEnabled()) && !message.getActionType().isQuery()) {
                    messageCanceller.cancelOperation("Exception in service: " + serviceName + " " + e.getMessage(), message.getOperationId());
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

    public void cancelMessage(NormalizedMessage message) throws Exception {
        String eventId = message.getObjectId();
        NormalMessage canceledEvent = messageStorage.getByID(eventId);
        if (canceledEvent == null) {
            log.error("Operation with id {} cannot be cancelled because it is not stored before.", eventId);
            return;
        }
        var cachedHandler = getHandler(mapper.normalize(canceledEvent));
        if (cachedHandler instanceof CancelableMessageHandler cancelableMessageHandler) {
            cancelableMessageHandler.cancel(canceledEvent);
        }
    }

    private MessageHandler getHandler(NormalizedMessage message) {
        IMessagingDomain domain = message.getDomain();
        IEventAction action = message.getActionType();
        var keyPair = Pair.of(domain, action);
        log.debug("Getting handler for domain {} and action {}", domain, action);
        log.debug("Handlers cache:{}", cache);
        log.debug("Handlers list:{}", handlers);
        return cache.computeIfAbsent(keyPair,
                key ->
                        handlers.stream().filter(handler -> handler.canHandle(key.getFirst(), key.getSecond()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("No handler found for keyPair: " + keyPair)));
    }

}
