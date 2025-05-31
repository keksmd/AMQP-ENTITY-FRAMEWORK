package dada.tuda.framework.crud.listening;

import dada.tuda.framework.facade.MessagingEntittyRepository;
import dada.tuda.framework.normalization.types.interfaces.EntityProducer;
import dada.tuda.framework.normalization.types.realizations.MessagingEntityRepositoryInvocationHandler;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;

/**
 * Фабрика, которая под капотом создаёт JDK-прокси
 * для интерфейса MessagingEntittyRepository<T>.
 */
public class MessagingRepositoryFactoryBean<T>
        implements FactoryBean<MessagingEntittyRepository<T>>,
        ApplicationContextAware,
        InitializingBean {

    @Setter
    private Class<T> entityType;
    @Setter
    private Class<? extends MessagingEntittyRepository<T>> repositoryInterface;

    private ApplicationContext ctx;
    private MessagingEntittyRepository<T> proxy;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        EntityProducer<T> producer = ctx.getBean("entityProducer", EntityProducer.class);
        Class<?> repoIface = repositoryInterface;
        this.proxy = (MessagingEntittyRepository<T>) Proxy.newProxyInstance(
                repoIface.getClassLoader(),
                new Class[]{ repoIface },
                new MessagingEntityRepositoryInvocationHandler<>(producer, entityType)
        );
    }

    @Override
    public MessagingEntittyRepository<T> getObject() {
        return this.proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.repositoryInterface;
    }
}