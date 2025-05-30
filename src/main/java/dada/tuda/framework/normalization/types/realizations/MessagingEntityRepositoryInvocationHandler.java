package dada.tuda.framework.normalization.types.realizations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * InvocationHandler для прокси MessagingEntittyRepository<T>
 */
public class MessagingEntityRepositoryInvocationHandler<T> implements InvocationHandler {
    private final EntityProducer<T> producer;
    private final Class<T> entityType;

    public MessagingEntityRepositoryInvocationHandler(EntityProducer<T> producer, Class<T> entityType) {
        this.producer = producer;
        this.entityType = entityType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(producer, args);
    }
}
