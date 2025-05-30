package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapStoragingQueueNameContext implements QueueNameContext {


    Map<IMessagingDomain, List<String>> contextNames = new ConcurrentHashMap<>();


    @Override
    public List<String> getQueueNameListByDomain(IMessagingDomain domain) {
        var names = contextNames.get(domain);
        return names == null ? new ArrayList<>() : names;

    }


    @Override
    public void registerQueueNameForDomain(String queue, IMessagingDomain domain) {
        contextNames.computeIfAbsent(domain, domain1 -> new ArrayList<>()).add(queue);
    }

}
