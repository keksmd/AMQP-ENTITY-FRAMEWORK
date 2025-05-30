package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.Set;

public interface DomainContext {
    IMessagingDomain getByName(String domain);

    Set<IMessagingDomain> getAllDomains();

    void registerDomain(IMessagingDomain domain);
}
