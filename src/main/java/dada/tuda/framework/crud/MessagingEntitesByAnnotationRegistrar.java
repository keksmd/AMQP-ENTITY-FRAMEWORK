package dada.tuda.framework.crud;

import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.QueueAnnotationContext;
import dada.tuda.framework.crud.extractor.ActorId;
import dada.tuda.framework.crud.extractor.ObjectId;
import dada.tuda.framework.crud.extractor.OperationId;
import dada.tuda.framework.crud.extractor.PayloadMap;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.lang.reflect.Field;


@RequiredArgsConstructor
public class MessagingEntitesByAnnotationRegistrar<T> implements BeanDefinitionRegistryPostProcessor {
    private final EntityContext entityContext;
    private final DomainContext domainContext;
    private final QueueAnnotationContext queueContext;


    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }

            Class<T> beanClass = (Class<T>) Class.forName(beanClassName);
            if (beanClass.isAnnotationPresent(MessagingEntity.class)) {

                MessagingEntity domainAnnotzated = beanClass.getAnnotation(MessagingEntity.class);
                String domainName = domainAnnotzated.domain();
                IMessagingDomain domain = domainContext.getByName(domainName);

                if (domain == null) {
                    SimpleDomain newDomain = (new SimpleDomain(domainName));
                    newDomain.setCreateDefaultBindings(Boolean.TRUE.toString().equals(domainAnnotzated.createDefaultBindings())).setTtl(Long.parseLong(domainAnnotzated.getTtl()));
                    domainContext.registerDomain(newDomain);
                    domain = newDomain;
                }

                entityContext.registerDomainMembership(domain, beanClass);

                for (Field f : beanClass.getDeclaredFields()) {
                    if (f.isAnnotationPresent(ObjectId.class)) {
                        entityContext.registerObjectIdExtractor(o -> {
                            try {
                                f.setAccessible(true);
                                Object value = f.get(o);
                                if (value == null) {
                                    return null;
                                }
                                if (f.getType().isPrimitive()) {
                                    return String.valueOf(value);
                                } else if (f.getType().equals(String.class)) {
                                    return (String) value;
                                }
                                return f.get(o).toString();
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }, beanClass, f.getName());
                    }
                    if (f.isAnnotationPresent(ActorId.class)) {
                        entityContext.registerActorIdExtractor(o -> {
                            try {
                                f.setAccessible(true);
                                Object value = f.get(o);
                                if (value == null) {
                                    return null;
                                }
                                if (f.getType().isPrimitive()) {
                                    return String.valueOf(value);
                                } else if (f.getType().equals(String.class)) {
                                    return (String) value;
                                }
                                return f.get(o).toString();
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }, beanClass, f.getName());
                    }
                    if (f.isAnnotationPresent(OperationId.class)) {
                        entityContext.registerOperationIdExtractor(o -> {
                            try {
                                f.setAccessible(true);
                                Object value = f.get(o);
                                if (value == null) {
                                    return null;
                                }
                                if (f.getType().isPrimitive()) {
                                    return String.valueOf(value);
                                } else if (f.getType().equals(String.class)) {
                                    return (String) value;
                                }
                                return f.get(o).toString();
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }, beanClass, f.getName());
                    }
                    if (f.isAnnotationPresent(PayloadMap.class)) {
                        entityContext.registerPayloadMapExtractor(o -> {
                            try {
                                f.setAccessible(true);
                                return f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }, beanClass, f.getName(), f.getAnnotation(PayloadMap.class));
                    }
                }

                Queue[] queues = domainAnnotzated.queues();
                if (queues != null) {
                    for (Queue queue : queues) {
                        if (queue != null) {
                            queueContext.registerQueueForDomain(queue, domain);
                        }
                    }
                }
            }
        }
    }

}

