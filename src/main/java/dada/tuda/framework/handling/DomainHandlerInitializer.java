package dada.tuda.framework.handling;

import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DomainHandlerInitializer implements SmartInitializingSingleton {
    private final DomainContext domainContext;
    private final ApplicationContext ctx;
    private final HandlerContext handlerContext;
    private final IEventActionContext eventActionContext;
    private final Environment environment;
    private final PayloadConverter payloadConverter;

    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(DomainHandlers.class);
        log.debug("Found {} beans annotated with @DomainHandlers", beans.size());
        for (Object bean : beans.values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            DomainHandlers annotation = beanClass.getAnnotation(DomainHandlers.class);
            String domainName = environment.resolvePlaceholders(annotation.domain()).trim();
            if (domainName.isBlank()) {
                log.warn("Domain name resolved to blank for bean class {}. Skipping.", beanClass.getName());
                continue;
            }
            IMessagingDomain domain = domainContext.getByName(domainName);
            for (Method classMethod : beanClass.getDeclaredMethods()) {
                if (!classMethod.isAnnotationPresent(ActionHandler.class)) {
                    continue;
                }
                ActionHandler actionAnnotated = classMethod.getAnnotation(ActionHandler.class);
                String[] actions = actionAnnotated.action();
                if (actions == null || actions.length == 0) {
                    log.warn("No actions defined in @ActionHandler on method {}. Skipping.", classMethod.getName());
                    continue;
                }
                for (String action : actions) {
                    String resolvedAction = environment.resolvePlaceholders(action).trim();
                    if (resolvedAction.isBlank()) {
                        log.warn("Resolved action is blank for method {} in class {}", classMethod.getName(), beanClass.getName());
                        continue;
                    }
                    classMethod.setAccessible(true);
                    var actionType = eventActionContext.getByName(resolvedAction);
                    if (actionType == null) {
                        log.warn("Action type '{}' not found for method {}. Skipping.", resolvedAction, classMethod.getName());
                        continue;
                    }
                    String cancelName = actionAnnotated.cancelMethod();
                    Method cancelMethod = null;

                    for (Method cancelCandidate : beanClass.getDeclaredMethods()) {
                        if (cancelName != null && !cancelName.isBlank()) {
                            if (cancelCandidate.getName().equals(cancelName)) {
                                cancelMethod = cancelCandidate;
                                break;
                            }
                        } else if (cancelCandidate.getName().equals(resolvedAction + "Cancel")) {
                            cancelMethod = cancelCandidate;
                        }
                    }

                    if (cancelMethod != null) {
                        log.debug("Registering cancelable handler for action '{}' with cancel method '{}'", resolvedAction, cancelMethod.getName());
                        handlerContext.addHandler(domain, actionType, new CancelableMessageHandlerAdapter(classMethod, bean, payloadConverter));
                        handlerContext.addHandler(domain, eventActionContext.getOrCreateCancelByAction(actionType),
                                new CancelableMessageHandlerAdapter(cancelMethod, bean, payloadConverter));
                    } else {
                        log.debug("Registering handler for action '{}' without cancel method", resolvedAction);
                        handlerContext.addHandler(domain, actionType,
                                new CancelableMessageHandlerAdapter(classMethod, bean, payloadConverter));

                    }
                }
            }
        }
    }

}


