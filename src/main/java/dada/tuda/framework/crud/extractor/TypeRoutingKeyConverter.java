package dada.tuda.framework.crud.extractor;

import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TypeRoutingKeyConverter implements RoutingKeyConverter {
    private final List<IMessagingDomain> domains;

    @Override
    public String toRoutingKey(NormalizedMessage message) {
        return message.getDomain().getKey() + "." + message.getActionType().name().toLowerCase();
    }

    @Override
    public String toRoutingKey(IMessagingDomain domain, IEventAction action) {
        return domain.getKey() + "." + action.name().toLowerCase();
    }

    @Override
    public IMessagingDomain getDomainByRoutingKey(String routingKey) {
        return domains.stream().filter(domain -> domain.getKey().equals(routingKey.split("\\.")[0])).findFirst().orElseThrow(IllegalStateException::new);
    }
}
