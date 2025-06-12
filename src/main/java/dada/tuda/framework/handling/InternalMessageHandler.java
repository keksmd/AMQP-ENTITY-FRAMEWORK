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
        if (domain != null && action != null && eventActionContext.isAllowedAction(domain, action)) {
            if (normalizedMessage.getActionType() instanceof CancelEventActionTemplate) {
                String reason = (String) normalizedMessage.getPayloadMap().get("reason");
                if (reason != null) {
                    log.warn("operation with id={} is cancel. Target message is {} \nReason: {}", normalizedMessage.getOperationId(), normalizedMessage.getObjectId(), reason);
                }
                this.cancelMessage(normalizedMessage);
                return null;
            }
            boolean needsCancel = false;
            String msg = "";
            if (!messageStorage.isProcessed(normalizedMessage)) {
                try {
                    CancelableMessageHandlerAdapter handler = getHandler(normalizedMessage);
                    var ans = handler.handle(normalizedMessage, raw);
                    if ((!properties.getMessaging().isStoreOnlyCancelable() || handler instanceof CancelableMessageHandler) && messageStorage.isEnabled()) {
                        messageStorage.storeEventAsProcessed(normalizedMessage);
                    }
                    if (normalizedMessage.getActionType().isQuery()) {
                        return ans;
                    }

                } catch (Exception e) {
                    log.warn("operation {} should be canceled: \n {}", normalizedMessage.getOperationId(), e.getCause() != null ? e.getMessage() + ": " + e.getCause().getMessage() : e.getMessage());
                    if (properties.getMessaging().getSaga().isEnabled()
                        && !normalizedMessage.getActionType().isQuery()
                        && normalizedMessage.getActionType().isCancelable()
                        && !(normalizedMessage.getActionType() instanceof CancelEventActionTemplate)) {
                        needsCancel = true;
                        msg = e.getCause() == null ? e.getMessage() : (e.getMessage() + ": " + e.getCause().getMessage());
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
        CancelableMessageHandlerAdapter cachedHandler = getHandler(mapper.normalize(canceledEvent));

        log.debug("Handling cancel for  {}.", canceledEvent);
        cachedHandler.cancel(mapper.normalize(canceledEvent));
    }

    private CancelableMessageHandlerAdapter getHandler(NormalizedMessage message) {
        IMessagingDomain domain = message.getDomain();
        IEventAction action = message.getActionType();
        log.debug("Getting handler for domain {} and action {}", domain, action);
        return handlerContext.getHandler(message);
    }

}
