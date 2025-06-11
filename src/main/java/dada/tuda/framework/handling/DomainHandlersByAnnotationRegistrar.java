package dada.tuda.framework.handling;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;


@RequiredArgsConstructor
public class DomainHandlersByAnnotationRegistrar implements BeanFactoryPostProcessor, Ordered {
    private final DomainContext domainContext;
    private final IEventActionContext eventActionContext;
    private final HandlerContext handlerContext;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return 100;
    }

    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            Class<?> beanClass = Class.forName(beanClassName);
            if (beanClass.isAnnotationPresent(DomainHandlers.class)) {

                DomainHandlers domainAnnotzated = beanClass.getAnnotation(DomainHandlers.class);
                String domainName = domainAnnotzated.domain();
                IMessagingDomain domain = domainContext.getByName(domainName);
                for (Method f : beanClass.getDeclaredMethods()) {
                    if (f.isAnnotationPresent(ActionHandler.class)) {
                        ActionHandler actionAnnotzated = beanClass.getAnnotation(ActionHandler.class);
                        String[] actions = actionAnnotzated.action();
                        if (actions != null) {
                            for (String action : actions) {
                                f.setAccessible(true);
                                var actionType = eventActionContext.getByName(action);
                                handlerContext.addHandler(domain, actionType, new CancelableMessageHandlerAdapter(actionType, domain, objectMapper, f, beanFactory.getBean(beanName)));
                            }
                        }
                    }
                }
            }
        }
    }
}

