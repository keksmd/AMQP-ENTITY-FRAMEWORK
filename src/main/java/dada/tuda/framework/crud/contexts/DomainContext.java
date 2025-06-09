package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.Collection;

public interface DomainContext {
    IMessagingDomain getByName(String domain);

    Collection<IMessagingDomain> getAllDomains();

    void registerDomain(IMessagingDomain domain);

    default void init() {
    }
}
