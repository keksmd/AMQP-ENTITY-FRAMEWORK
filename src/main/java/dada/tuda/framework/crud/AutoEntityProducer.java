package dada.tuda.framework.crud;

import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.normalization.types.realizations.EntityProducer;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.ResolvableType;

import java.util.List;

public class AutoEntityProducer implements BeanDefinitionRegistryPostProcessor {
    private final List<Class<?>> entityTypes;

    public AutoEntityProducer(EntityContext entityContext) {
        entityTypes = entityContext.getAllTypes();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
        for (Class<?> entityClass : entityTypes) {
            ResolvableType targetType = ResolvableType.forClassWithGenerics(EntityProducer.class, entityClass);
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(EntityProducer.class)
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                    .getBeanDefinition();
            // Присваиваем тип интерфейса
            beanDefinition.setAttribute("factoryBeanObjectType", targetType);

            String beanName = entityClass.getSimpleName().substring(0, 1).toLowerCase()
                              + entityClass.getSimpleName().substring(1) + "MessagingRepository";
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no-op
    }
}
