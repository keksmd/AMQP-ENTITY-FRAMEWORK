package dada.tuda.framework.handling;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;


@RequiredArgsConstructor
public class DomainHandlerInitializer implements SmartInitializingSingleton {
    private final DomainContext domainContext;
    private final ApplicationContext ctx;
    private final HandlerContext handlerContext;
    private final IEventActionContext eventActionContext;
    private final ObjectMapper objectMapper;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(DomainHandlers.class);
        for (Object bean : beans.values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            DomainHandlers annotation = beanClass.getAnnotation(DomainHandlers.class);
            String domainName = annotation.domain();
            IMessagingDomain domain = domainContext.getByName(domainName);
            for (Method classMethod : beanClass.getDeclaredMethods()) {
                if (classMethod.isAnnotationPresent(ActionHandler.class)) {
                    ActionHandler actionAnnotzated = classMethod.getAnnotation(ActionHandler.class);
                    String[] actions = actionAnnotzated.action();
                    if (actions != null) {
                        for (String action : actions) {
                            classMethod.setAccessible(true);
                            var actionType = eventActionContext.getByName(action);
                            String cancelName = actionAnnotzated.cancelMethod();
                            Method cancelMethod = null;

                            for (Method cancelCandidate : beanClass.getDeclaredMethods()) {
                                if (cancelName != null && !cancelName.isBlank() && cancelMethod == null && cancelCandidate.getName().equals(action + "Cancel")) {
                                    cancelMethod = cancelCandidate;
                                }
                                if (cancelCandidate.getName().equals(cancelName)) {
                                    cancelMethod = cancelCandidate;
                                }
                            }

                            if (cancelMethod != null) {
                                handlerContext.addHandler(domain, actionType,
                                        new CancelableMessageHandlerAdapter(actionType, domain, objectMapper, classMethod, cancelMethod, bean));

                            } else {
                                handlerContext.addHandler(domain, actionType,
                                        new CancelableMessageHandlerAdapter(actionType, domain, objectMapper, classMethod, bean));

                            }
                        }
                    }
                }
            }
        }
    }
}


