package dada.tuda.framework.crud.extractor;

import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TypeRoutingKeyConverter implements RoutingKeyConverter {
    private final DomainContext domainContext;

    @Override
    public String toRoutingKey(NormalizedMessage message) {
        return message.getDomain().getKey() + "." + message.getActionType().getName().toLowerCase();
    }

    @Override
    public String toRoutingKey(IMessagingDomain domain, IEventAction action) {
        return domain.getKey() + "." + action.getName().toLowerCase();
    }

    @Override
    public IMessagingDomain getDomainByRoutingKey(String routingKey) {
        return domainContext.getAllDomains().stream().filter(domain -> domain.getKey().equals(routingKey.split("\\.")[0])).findFirst().orElseThrow(IllegalStateException::new);
    }
}
