package dada.tuda.framework.handling;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.contexts.HandlerContext;
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
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;


@RequiredArgsConstructor
@Slf4j
public class InternalMessageHandler {
    private final MessageStorage messageStorage;
    private final MessageCanceller messageCanceller;
    private final MessageMapper mapper;
    private final IEventActionContext eventActionContext;
    private final HandlerContext handlerContext;
    private final ObjectProvider<DadaTudaFrameworkProperties> provider;
    private DadaTudaFrameworkProperties properties;
    @Value("${spring.application.name}")
    private String serviceName;

    @PostConstruct
    void init() {
        properties = provider.getIfAvailable();
    }

    public Object handleMessage(NormalMessage message, Message raw) throws Exception {
        return this.handleIntenal(mapper.normalize(message), raw);
    }

    public Object handleIntenal(NormalizedMessage normalizedMessage, Message raw) throws Exception {
        var domain = normalizedMessage.getDomain();
        var action = normalizedMessage.getActionType();

        if (domain == null || action == null) {
            log.warn("Normalized message has null domain or action: domain={}, action={}", domain, action);
            return null;
        }
        if (!eventActionContext.isAllowedAction(domain, action)) {
            log.warn("Action {} in domain {} is not allowed by eventActionContext.", action, domain);
            return null;
        }
        if (action instanceof CancelEventActionTemplate) {
            String reason = (String) normalizedMessage.getPayloadMap().get("reason");
            if (reason != null) {
                log.warn("Operation with id={} is being cancelled. Target message: {}. Reason: {}",
                        normalizedMessage.getOperationId(), normalizedMessage.getObjectId(), reason);
            }
            this.cancelMessage(normalizedMessage);
            return null;
        }
        if (messageStorage.isProcessed(normalizedMessage)) {
            log.warn("Operation already processed: {}", normalizedMessage.getOperationId());
            return null;
        }
        boolean needsCancel = false;
        String cancelMessage = "";
        try {
            CancelableMessageHandlerAdapter handler = getHandler(normalizedMessage);
            Object result = handler.handle(normalizedMessage, raw);
            if (action.isQuery()) {
                return result;
            }

        } catch (Exception e) {
            String errorMessage = e.getCause() != null
                    ? e.getMessage() + ": " + e.getCause().getMessage()
                    : e.getMessage();
            log.warn("Exception during handling of operation {}. Suggesting cancel. Error: {}",
                    normalizedMessage.getOperationId(), errorMessage);

            if (properties.getMessaging().getSaga().isEnabled()
                && !action.isQuery()
                && action.isCancelable()) {
                needsCancel = true;
                cancelMessage = errorMessage;
            } else {
                throw e; // Re-throw if not eligible for cancel
            }
        }
        messageStorage.storeEventAsProcessed(normalizedMessage);
        if (needsCancel) {
            log.warn("Triggering cancel for operation {} in domain {} due to internal exception.",
                    normalizedMessage.getOperationId(), domain);
            messageCanceller.cancelOperation("Exception in service: " + serviceName + " " + cancelMessage,
                    action,
                    normalizedMessage.getOperationId(),
                    domain);
        }

        return null;
    }

    public <T> void cancelMessage(NormalizedMessage message) throws Exception {
        String eventId = message.getObjectId();
        NormalMessage canceledEvent = messageStorage.getByID(eventId);

        if (canceledEvent == null) {
            log.error("Cannot cancel operation with id {} because it was not found in storage.", eventId);
            return;
        }

        NormalizedMessage normalized = mapper.normalize(canceledEvent);
        CancelableMessageHandlerAdapter handler = getHandler(message);
        if (handler == null) {
            log.error("No handler found for canceling operation with id {} in domain {} and action {}.",
                    eventId, message.getDomain(), message.getActionType());
            return;
        }

        log.debug("Handling cancel for stored event: {}", canceledEvent);
        handler.cancel(normalized);
    }

    private CancelableMessageHandlerAdapter getHandler(NormalizedMessage message) {
        IMessagingDomain domain = message.getDomain();
        IEventAction action = message.getActionType();
        log.debug("Getting handler for domain {} and action {}", domain, action);
        return handlerContext.getHandler(message);
    }

}
