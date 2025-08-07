package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.MessagingEntityDescriptor;
import dada.tuda.framework.crud.extractor.PayloadMap;
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

    private final Map<String, MessagingEntityDescriptor> context = new ConcurrentHashMap<>();


    @Override
    public <T> void registerDomainMembership(IMessagingDomain domain, Class<T> clazz) {
        context.computeIfAbsent(clazz.getName(), key -> new MessagingEntityDescriptor())
                .setDomain(domain);
    }

    @Override
    public <T> void registerObjectIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz.getName(), key -> new MessagingEntityDescriptor());
        descriptor.setObjectIdExtractor(domainExtractor);
        descriptor.setObjectIdFiled(field);
    }

    @Override
    public <T> void registerActorIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz.getName(), key -> new MessagingEntityDescriptor());
        descriptor.setActorIdExtractor(domainExtractor);
        descriptor.setActorIdField(field);
    }

    @Override
    public <T> void registerOperationIdExtractor(Function<Object, String> domainExtractor, Class<T> clazz, String field) {
        var descriptor = context.computeIfAbsent(clazz.getName(), key -> new MessagingEntityDescriptor());
        descriptor.setOperationIdExtractor(domainExtractor);
        descriptor.setOperationIdFiled(field);
    }


    @Override
    public <T> MessagingEntityDescriptor getDescriptorByMessagingEntityClass(Class<T> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        var descriptor = context.get(clz.getName());
        return descriptor;
    }

    @Override
    public List<String> getAllTypes() {
        return new ArrayList<>(context.keySet());
    }

    @Override
    public <T> void registerPayloadMapExtractor(Function<Object, Object> payLoadExtractor, Class<T> clazz, String filedName, PayloadMap an) {
        var descriptor = context.computeIfAbsent(clazz.getName(), key -> new MessagingEntityDescriptor());
        descriptor.setPayLoadExtractor(payLoadExtractor);
        descriptor.setPayload(filedName);
        descriptor.setPayloadAnnotation(an);

    }
}

