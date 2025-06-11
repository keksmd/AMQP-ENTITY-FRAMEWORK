package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.rabbit.annotation.Queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapStoragingQueueAnnotationContext implements QueueAnnotationContext {


    Map<IMessagingDomain, List<Queue>> contextNames = new ConcurrentHashMap<>();




    @Override
    public List<Queue> getQueueListByDomain(IMessagingDomain domain) {
        return contextNames.get(domain);
    }


    @Override
    public void registerQueueForDomain(Queue queue, IMessagingDomain domain) {
        contextNames.computeIfAbsent(domain, domain1 -> new ArrayList<>()).add(queue);
    }

}
