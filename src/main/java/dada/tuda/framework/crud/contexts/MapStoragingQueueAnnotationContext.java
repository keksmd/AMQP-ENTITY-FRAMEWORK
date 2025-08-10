package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.rabbit.annotation.Queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapStoragingQueueAnnotationContext implements QueueAnnotationContext {


    Map<IMessagingDomain, Queue> contextNames = new ConcurrentHashMap<>();


    @Override
    public Queue getQueueByDomain(IMessagingDomain domain) {
        return contextNames.get(domain);
    }


    @Override
    public void registerQueueForDomain(Queue queue, IMessagingDomain domain) {
        contextNames.put(domain, queue);
    }

}
