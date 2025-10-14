package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.core.Queue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapStoragingQueueContext implements QueueContext {


    private final Map<IMessagingDomain, Queue> contextNames = new ConcurrentHashMap<>();


    @Override
    public Queue getQueueByDomain(IMessagingDomain domain) {
        return contextNames.get(domain);
    }


    @Override
    public void registerQueueForDomain(Queue queue, IMessagingDomain domain) {
        contextNames.put(domain, queue);
    }

    @Override
    public Set<Queue> getAllQueues() {
        return new HashSet<>(contextNames.values());
    }

}
