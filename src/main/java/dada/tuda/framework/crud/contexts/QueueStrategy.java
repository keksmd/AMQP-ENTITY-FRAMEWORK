package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

public interface QueueStrategy {
    String createQueueNameForDomain(IMessagingDomain domain);

}
