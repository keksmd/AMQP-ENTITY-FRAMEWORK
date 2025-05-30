package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;

public interface QueueStrategy {
    List<String> getQueuesByDomain(IMessagingDomain domain);

}
