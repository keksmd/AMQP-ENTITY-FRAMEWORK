package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
public class AnnotationDomainContext implements DomainContext {
    private final ConcurrentHashMap<String, IMessagingDomain> map = new ConcurrentHashMap<>();

    @Override
    public IMessagingDomain getByName(String domainName) {
        return map.get(domainName);
    }

    @Override
    public Collection<IMessagingDomain> getAllDomains() {
        return map.values();
    }

    @Override
    public void registerDomain(IMessagingDomain domain) {
        map.put(domain.getName(), domain);
    }
}
