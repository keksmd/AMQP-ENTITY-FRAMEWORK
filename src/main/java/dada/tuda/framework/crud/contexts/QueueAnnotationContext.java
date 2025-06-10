package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.ListenableQueue;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;

public interface QueueAnnotationContext {

    List<ListenableQueue> getQueueListByDomain(IMessagingDomain domain);

    void registerQueueForDomain(ListenableQueue queue, IMessagingDomain domain);
}
