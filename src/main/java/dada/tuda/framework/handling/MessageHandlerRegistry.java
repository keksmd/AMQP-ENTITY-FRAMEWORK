package dada.tuda.framework.handling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class MessageHandlerRegistry {
    private final MessageStorage messageStorage;
    private final MessageCanceller messageCanceller;
    private final MessageMapper mapper;
    private final IEventActionContext eventActionContext;
    private final List<MessageHandler> handlers;
    private final ObjectProvider<DadaTudaFrameworkProperties> provider;
    private final ObjectMapper objectMapper;
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

    public <T> Object handleIntenal(NormalizedMessage normalizedMessage) throws Exception {
        var domain = normalizedMessage.getDomain();
        var action = normalizedMessage.getActionType();
        if (domain != null && action != null && eventActionContext.isAllowedAction(domain, action)) {
            if (normalizedMessage.getActionType() instanceof CancelEventActionTemplate) {
                String reason = (String) normalizedMessage.getPayloadMap().get("reason");
                if (reason != null) {
                    log.warn("operation with id={} is cancel. Target message is {} \nReason: {}", normalizedMessage.getOperationId(), normalizedMessage.getObjectId(), reason);
                }
                this.cancelMessage(normalizedMessage);
                return null;
            }
            MessageHandler<T> cachedHandler = (MessageHandler<T>) getHandler(normalizedMessage);
            boolean needsCancel = false;
            String msg = "";
            if (!messageStorage.isProcessed(normalizedMessage)) {
                try {
                    Object returned = cachedHandler.handle(normalizedMessage, (T) objectMapper.convertValue(normalizedMessage.getPayloadMap(), getGenericParameterType(cachedHandler)));
                    if ((!properties.getMessaging().isStoreOnlyCancelable() || cachedHandler instanceof CancelableMessageHandler) && messageStorage.isEnabled()) {
                        messageStorage.storeEventAsProcessed(normalizedMessage);
                    }
                    if (normalizedMessage.getActionType().isQuery()) {
                        return returned;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    log.warn("operation {} should be canceled: \n {}", normalizedMessage.getOperationId(), e.getMessage());
                    if (properties.getMessaging().getSaga().isEnabled()
                        && !normalizedMessage.getActionType().isQuery()
                        && normalizedMessage.getActionType().isCancelable()
                        && !(normalizedMessage.getActionType() instanceof CancelEventActionTemplate)) {
                        needsCancel = true;
                        msg = e.getMessage();
                    } else {
                        throw e;
                    }
                } finally {
                    this.messageStorage.storeEventAsProcessed(normalizedMessage);
                }
                if (needsCancel) {
                    messageCanceller.cancelOperation("Exception in service: " + serviceName + " " + msg,
                            normalizedMessage.getActionType(),
                            normalizedMessage.getOperationId(),
                            normalizedMessage.getDomain());
                }
                return null;
            } else {
                log.warn("operation already processed: {}", normalizedMessage.getOperationId());
                return null;
            }
        } else {
            log.warn("Action {} in domain {} is not allowed by eventActionContext.", action, domain);
            return null;
        }
    }

    public <T> void cancelMessage(NormalizedMessage message) throws Exception {
        String eventId = message.getObjectId();
        NormalMessage canceledEvent = messageStorage.getByID(eventId);
        if (canceledEvent == null) {
            log.error("Operation with id {} cannot be cancelled because it is not stored before.", eventId);
            return;
        }
        var cachedHandler = getHandler(mapper.normalize(canceledEvent));
        if (cachedHandler instanceof CancelableMessageHandler) {
            CancelableMessageHandler<T> cancelableMessageHandler = (CancelableMessageHandler<T>) cachedHandler;
            log.debug("Handling cancel for  {}.", canceledEvent);
            cancelableMessageHandler.cancel(canceledEvent, objectMapper.convertValue(canceledEvent.getPayloadMap(), new TypeReference<T>() {
            }));
        }
    }

    private MessageHandler getHandler(NormalizedMessage message) {
        IMessagingDomain domain = message.getDomain();
        IEventAction action = message.getActionType();
        log.debug("Getting handler for domain {} and action {}", domain, action);

        return handlers.stream().filter(handler -> handler.canHandle(message))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No handler found for message %s".formatted(message)));
    }

    public static Class<?> getGenericParameterType(Object handler) {
        Type[] interfaces = handler.getClass().getGenericInterfaces();
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType pType &&
                pType.getRawType().equals(MessageHandler.class)) {
                return (Class<?>) pType.getActualTypeArguments()[0];
            }
        }
        throw new IllegalStateException("Cannot resolve generic type");
    }

}
