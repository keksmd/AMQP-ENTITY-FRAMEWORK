package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.handling.CancelableMessageHandlerAdapter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerContext {
    private final Map<String, Map<String, CancelableMessageHandlerAdapter>> contextHandlers;
    /**
     * Internal storage mapping each domain to the set of actions that have at least one registered handler.
     */
    private final Map<IMessagingDomain, Set<IEventAction>> actionsWithHandler = new ConcurrentHashMap<>();

    public HandlerContext() {
        this.contextHandlers = new ConcurrentHashMap<>();
    }

    public CancelableMessageHandlerAdapter getHandler(NormalMessage message) {
        String domain = message.getDomainName();
        String action = message.getActionTypeName();
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
        Map<String, CancelableMessageHandlerAdapter> acts = contextHandlers.computeIfAbsent(domain.getName(), k -> new ConcurrentHashMap<>());
        acts.putIfAbsent(action.getName(), handler);
        actionsWithHandler.computeIfAbsent(domain, d -> new HashSet<>()).add(action);
    }

    public Set<IEventAction> getActionsWithHandler(IMessagingDomain domain) {
        return actionsWithHandler.getOrDefault(domain, Collections.emptySet());
    }
}
