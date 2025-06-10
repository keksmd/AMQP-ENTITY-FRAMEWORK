package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.MessagingEntityDescriptor;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;
import java.util.function.Function;

public interface EntityContext {


    <T> void registerDomainMembership(IMessagingDomain domain, Class<T> clazz);

    <T> void registerObjectIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field);

    <T> void registerActorIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field);

    <T> void registerOperationIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field);

    <T> MessagingEntityDescriptor getDescriptorByMessagingEntityClass(Class<T> clz);


    List<Class<?>> getAllTypes();

    <T> void registerPayloadMapExtractor(Function<Object, Object> objectStringFunction, Class<T> beanClass, String name);
}
