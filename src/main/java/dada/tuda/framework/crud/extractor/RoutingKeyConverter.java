package dada.tuda.framework.crud.extractor;

import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

public interface RoutingKeyConverter {
    String toRoutingKey(NormalizedMessage message);

    String toRoutingKey(IMessagingDomain domain, IEventAction action);

    IMessagingDomain getDomainByRoutingKey(String routingKey);
}
