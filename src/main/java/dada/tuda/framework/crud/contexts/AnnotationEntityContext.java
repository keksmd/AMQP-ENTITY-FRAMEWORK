package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.MessagingEntityDescriptor;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class AnnotationEntityContext implements EntityContext {

    private final Map<Class<?>, MessagingEntityDescriptor> context = new ConcurrentHashMap<>();


    @Override
    public void registerDomainMembership(IMessagingDomain domain, Class clazz) {
        context.computeIfAbsent(clazz, key -> new MessagingEntityDescriptor())
                .setDomain(domain);
    }

    @Override
    public void registerObjectIdExtractor(Function<Object, String> domainExtractor, Class clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz, key -> new MessagingEntityDescriptor());
        descriptor.setObjectIdExtractor(domainExtractor);
        descriptor.setObjectIdFiled(field);
    }

    @Override
    public void registerActorIdExtractor(Function<Object, String> domainExtractor, Class clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz, key -> new MessagingEntityDescriptor());
        descriptor.setActorIdExtractor(domainExtractor);
        descriptor.setActorIdField(field);
    }

    @Override
    public void registerOperationIdExtractor(Function<Object, String> domainExtractor, Class clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz, key -> new MessagingEntityDescriptor());
        descriptor.setOperationIdExtractor(domainExtractor);
        descriptor.setOperationIdFiled(field);
    }


    @Override
    public MessagingEntityDescriptor getDescriptorByMessagingEntityClass(Class<?> clz) {
        return context.get(clz);
    }

    @Override
    public List<Class<?>> getAllTypes() {
        return new ArrayList<>(context.keySet());
    }
}

