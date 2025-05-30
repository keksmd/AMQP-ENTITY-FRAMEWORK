package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AnnotationDomainContext implements DomainContext {
    private final Set<IMessagingDomain> domains;
    private final ConcurrentHashMap<String, IMessagingDomain> map = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        domains.forEach(domain -> map.put(domain.getName(), domain));
    }

    @Override
    public IMessagingDomain getByName(String domainName) {
        return map.get(domainName);
    }

    @Override
    public Set<IMessagingDomain> getAllDomains() {
        return domains;
    }

    @Override
    public void registerDomain(IMessagingDomain domain) {
        domains.add(domain);
        map.put(domain.getName(), domain);
    }

}
