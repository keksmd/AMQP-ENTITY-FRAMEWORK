package dada.tuda.framework.crud;

import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.QueueNameContext;
import dada.tuda.framework.crud.extractor.ActorId;
import dada.tuda.framework.crud.extractor.ObjectId;
import dada.tuda.framework.crud.extractor.OperaionId;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


@RequiredArgsConstructor
@Component
public class MessagingEntityBeanFactoryPostProcessor<T> implements BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor, Ordered {
    private final EntityContext entityContext;
    private final DomainContext domainContext;
    private final QueueNameContext queueContext;

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
                    newDomain
                            .setCreateDefaultBindings(Boolean.TRUE.toString().equals(domainAnnotzated.createDefaultBindings()))
                            .setTtl(Long.parseLong(domainAnnotzated.getTtl()));
                    registerDomain(newDomain, registry);
                    entityContext.registerDomainMembership(newDomain, beanClass);
                    domain = newDomain;
                } else {
                    entityContext.registerDomainMembership(domain, beanClass);
                }
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
                    if (f.isAnnotationPresent(OperaionId.class)) {
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
                }

                String[] queues = domainAnnotzated.queues();
                if (queues != null) {
                    for (String queue : queues) {
                        if (queue != null && !queue.isEmpty()) {
                            queueContext.registerQueueNameForDomain(queue, domain);
                        }

                    }
                }
            }

        }
    }

    void registerDomain(IMessagingDomain domain, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(IMessagingDomain.class, () -> domain);
        BeanDefinition exchangeBeanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition(IMessagingDomain.class.getName() + domain.getName(), exchangeBeanDefinition);
        domainContext.registerDomain(domain);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
