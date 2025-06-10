package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.ListenableQueue;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapStoragingQueueAnnotationContext implements QueueAnnotationContext {


    Map<IMessagingDomain, List<ListenableQueue>> contextNames = new ConcurrentHashMap<>();




    @Override
    public List<ListenableQueue> getQueueListByDomain(IMessagingDomain domain) {
        return contextNames.get(domain);
    }


    @Override
    public void registerQueueForDomain(ListenableQueue queue, IMessagingDomain domain) {
        contextNames.computeIfAbsent(domain, domain1 -> new ArrayList<>()).add(queue);
    }

}
