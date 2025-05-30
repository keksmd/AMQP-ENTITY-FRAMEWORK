package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;

public interface QueueNameContext {

    List<String> getQueueNameListByDomain(IMessagingDomain domain);


    void registerQueueNameForDomain(String queue, IMessagingDomain domain);
}
