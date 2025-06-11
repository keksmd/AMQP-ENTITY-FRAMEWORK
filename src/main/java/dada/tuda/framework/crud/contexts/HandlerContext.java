package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.handling.CancelableMessageHandlerAdapter;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerContext {
    private final Map<IMessagingDomain, Map<IEventAction, CancelableMessageHandlerAdapter>> contextHandlers;

    public HandlerContext() {
        this.contextHandlers = new ConcurrentHashMap<>();
    }

    public CancelableMessageHandlerAdapter getHandler(NormalizedMessage message) {
        var domain = message.getDomain();
        var action = message.getActionType();
        if (domain != null && action != null) {
            var acts = contextHandlers.get(domain);
            if (acts != null) {
                return acts.get(action);
            } else {
                throw new IllegalArgumentException("No handler found for  domain %s".formatted(domain));
            }
        } else {
            throw new IllegalArgumentException("No handler found for domain %s and action %s".formatted(domain, action));
        }
    }

    public void addHandler(IMessagingDomain domain, IEventAction action, CancelableMessageHandlerAdapter handler) {
        Map<IEventAction, CancelableMessageHandlerAdapter> acts = contextHandlers.computeIfAbsent(domain, k -> new ConcurrentHashMap<>());
        acts.putIfAbsent(action, handler);
    }

}
