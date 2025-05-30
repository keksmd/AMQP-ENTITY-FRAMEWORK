package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.MessagingEntityDescriptor;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;
import java.util.function.Function;

public interface EntityContext {


    void registerDomainMembership(IMessagingDomain domain, Class clazz);

    void registerObjectIdExtractor(Function<Object, String> domainExtractor, Class clazz);

    void registerActorIdExtractor(Function<Object, String> domainExtractor, Class clazz);

    void registerOperationIdExtractor(Function<Object, String> domainExtractor, Class clazz);


    MessagingEntityDescriptor getDescriptorByMessagingEntityClass(Class<?> clz);


    List<Class<?>> getAllTypes();
}
