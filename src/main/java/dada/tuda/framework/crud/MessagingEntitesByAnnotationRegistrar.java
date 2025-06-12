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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

@Slf4j
@RequiredArgsConstructor
public class MessagingEntitesByAnnotationRegistrar<T> implements BeanDefinitionRegistryPostProcessor {
    private final EntityContext entityContext;
    private final DomainContext domainContext;
    private final QueueAnnotationContext queueContext;
    private final Environment environment;


    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            try {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                String beanClassName = beanDefinition.getBeanClassName();
                if (beanClassName == null) {
                    log.debug("Bean '{}' has no class name, skipping.", beanName);
                    continue;
                }
                Class<T> beanClass;
                try {
                    beanClass = (Class<T>) Class.forName(beanClassName);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    log.warn("Failed to load class '{}' for bean '{}': {}", beanClassName, beanName, e.getMessage());
                    continue;
                }
                if (!beanClass.isAnnotationPresent(MessagingEntity.class)) {
                    continue;
                }
                MessagingEntity domainAnnotated = beanClass.getAnnotation(MessagingEntity.class);
                String domainName = environment.resolvePlaceholders(domainAnnotated.domain()).trim();
                if (domainName.isBlank()) {
                    log.warn("Resolved blank domain name for bean '{}', skipping.", beanName);
                    continue;
                }

                IMessagingDomain domain = domainContext.getByName(domainName);
                if (domain == null) {
                    log.debug("Creating new domain: {}", domainName);
                    domain = new SimpleDomain(domainName)
                            .setCreateDefaultBindings(Boolean.TRUE.toString().equals(domainAnnotated.createDefaultBindings()))
                            .setTtl(Long.parseLong(domainAnnotated.ttl()));
                    domainContext.registerDomain(domain);
                }

                entityContext.registerDomainMembership(domain, beanClass);

                for (Field field : beanClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        if (field.isAnnotationPresent(ObjectId.class)) {
                            log.debug("Registering ObjectId extractor for {}.{}", beanClass.getSimpleName(), field.getName());
                            entityContext.registerObjectIdExtractor(getExtractor(field), beanClass, field.getName());
                        }
                        if (field.isAnnotationPresent(ActorId.class)) {
                            log.debug("Registering ActorId extractor for {}.{}", beanClass.getSimpleName(), field.getName());
                            entityContext.registerActorIdExtractor(getExtractor(field), beanClass, field.getName());
                        }
                        if (field.isAnnotationPresent(OperationId.class)) {
                            log.debug("Registering OperationId extractor for {}.{}", beanClass.getSimpleName(), field.getName());
                            entityContext.registerOperationIdExtractor(getExtractor(field), beanClass, field.getName());
                        }
                        if (field.isAnnotationPresent(PayloadMap.class)) {
                            log.debug("Registering PayloadMap extractor for {}.{}", beanClass.getSimpleName(), field.getName());
                            entityContext.registerPayloadMapExtractor(o -> {
                                try {
                                    return field.get(o);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }, beanClass, field.getName(), field.getAnnotation(PayloadMap.class));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to register extractor for field {}.{}: {}", beanClass.getSimpleName(), field.getName(), e.getMessage(), e);
                    }
                }

                Queue[] queues = domainAnnotated.queues();
                if (queues != null) {
                    for (Queue queue : queues) {
                        if (queue != null) {
                            log.debug("Registering queue {} for domain {}", queue.name(), domainName);
                            queueContext.registerQueueForDomain(queue, domain);
                        }
                    }
                }

            } catch (Exception e) {
                log.warn("Unexpected error during processing bean '{}': {}", beanName, e.getMessage(), e);
            }
        }
    }

    private static <T> java.util.function.Function<T, String> getExtractor(Field field) {
        return o -> {
            try {
                Object value = field.get(o);
                if (value == null) return null;
                if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                    return String.valueOf(value);
                }
                return value.toString();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

}

