package dada.tuda.framework.handling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import dada.tuda.framework.crud.QueueAnnotationParser;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.QueueAnnotationContext;
import dada.tuda.framework.crud.contexts.QueueStrategy;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.listening.UniversalMessageListener;
import dada.tuda.framework.handling.conversion.RabbitHandlerArgumentResolverComposite;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class DomainHandlerInitializer implements SmartInitializingSingleton {
    /**
     * Context providing registered messaging domains.
     */
    private final DomainContext domainContext;
    /**
     * Spring application context used to locate beans annotated with {@link DomainHandlers}.
     */
    private final ApplicationContext ctx;
    /**
     * Central handler context where handlers are registered.
     */
    private final HandlerContext handlerContext;
    /**
     * Event action context used to resolve actions by name and enumerate allowed actions.
     */
    private final IEventActionContext iEventActionContext;

    /**
     * Composite used to resolve method parameters for handlers invoked via RabbitMQ.
     */
    private final RabbitHandlerArgumentResolverComposite rabbitHandlerArgumentResolverComposite;

    /**
     * Context providing queue annotations grouped by domain.
     */
    private final QueueAnnotationContext queueContext;

    /**
     * Context providing exchanges for domains.
     */
    private final ExchangeContext exchangeContext;
    /**
     * Rabbit administrator used to declare queues and bindings.
     */
    private final RabbitAdmin rabbitAdmin;

    /**
     * Strategy for naming queue.
     */
    private final QueueStrategy queueStrategy;
    /**
     * Converter from domain and action to routing key.
     */
    private final RoutingKeyConverter routingKeyConverter;

    /**
     * Factory for establishing AMQP connections.
     */
    private final ConnectionFactory connectionFactory;
    /**
     * Internal message handler delegate.
     */
    private final InternalMessageHandler internalMessageHandler;
    /**
     * Parser for queue annotations.
     */
    private final QueueAnnotationParser queueAnnotationParser;
    /**
     * Jackson object mapper for message deserialization.
     */
    private final ObjectMapper objectMapper;
    /**
     * Number of concurrent consumers per listener container.
     */
    private final int concurrentConsumers;
    /**
     * Maximum number of concurrent consumers per listener container.
     */
    private final int maxConcurrentConsumers;
    /**
     * Message converter used by listener adapter.
     */
    private final MessageConverter messageConverter;
    /**
     * Flag indicating whether to decompose routing key into domain and action when not provided.
     */
    private final boolean decompose;
    private final List<AbstractRabbitListenerEndpoint> endpoints = new ArrayList<>();
    private UniversalMessageListener delegate;
    private MessageListenerAdapter adapter;

    /**
     * Scans the application context for beans annotated with {@link DomainHandlers} and registers
     * their handler methods.
     */
    @Override
    public void afterSingletonsInstantiated() {
        delegate = new UniversalMessageListener(internalMessageHandler, objectMapper);
        adapter = new MessageListenerAdapter(delegate, "handleMessage") {
            @Override
            protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
                String routingKey = message.getMessageProperties().getReceivedRoutingKey();
                if ((routingKey != null) &&
                    (extractedMessage instanceof NormalMessage msg && (msg.getActionTypeName() == null || msg.getDomainName() == null) && decompose)
                    && (msg.getActionTypeName() == null || msg.getDomainName() == null)) {
                    if (routingKey.contains(".")) {
                        var split = routingKey.split("\\.");
                        if (msg.getDomainName() == null) {
                            msg.setDomainName(split[0]);
                        }
                        if (msg.getActionTypeName() == null) {
                            if (split.length == 2) {
                                msg.setActionTypeName(split[1]);
                            } else if (split.length == 3 && "cancel".equals(split[2])) {
                                msg.setActionTypeName(split[1] + "." + split[2]);
                            }
                        }
                    } else {
                        msg.setActionTypeName(routingKey);
                    }
                }

                return new Object[]{ extractedMessage };
            }
        };
        adapter.setMessageConverter(messageConverter);

        // find all beans annotated with @DomainHandlers
        Map<String, Object> beans = ctx.getBeansWithAnnotation(DomainHandlers.class);
        log.debug("Found {} beans annotated with @DomainHandlers", beans.size());
        for (Object bean : beans.values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            DomainHandlers annotation = beanClass.getAnnotation(DomainHandlers.class);

            String domainName = (annotation.domain());
            if (domainName.isBlank()) {
                log.warn("Domain name resolved to blank for bean class {}. Skipping.", beanClass.getName());
                continue;
            }
            IMessagingDomain domain = domainContext.getByName(domainName);
            if (domain == null) {
                throw new IllegalStateException("Domain '" + domainName + "' not found in DomainContext. Ensure it is registered before initializing handlers.");
            }
            try {
                log.debug("Processing domain: {}", domain.getName());

                if (CancelPayload.CANCEL_DOMAIN.equals(domain.getName())) {
                    log.debug("Skipping cancel domain: {}", domain.getName());
                    continue;
                }
                var exchange = exchangeContext.getExchange(domain);
                if (exchange == null) {
                    log.warn("No exchange found for domain: {}", domain.getName());
                    continue;
                }
                var queueAnnotation = queueContext.getQueueByDomain(domain);
                if (queueAnnotation == null) {
                    log.warn("No queue found for domain: {}", domain.getName());
                    continue;
                }
                log.debug("Found {} queue for domain: {}", queueAnnotation.name(), domain.getName());
                Set<IEventAction> withHandler = handlerContext.getActionsWithHandler(domain);

                if (withHandler.isEmpty()) {
                    log.warn("No actions with handlers found for domain: {}", domain.getName());
                    continue;
                }
                Queue rabbitQueue = initQueue(queueAnnotation, domain);

                for (var action : withHandler) {
                    try {
                        var routing = routingKeyConverter.toRoutingKey(domain, action);
                        Binding binding = BindingBuilder.bind(rabbitQueue).to(exchange).with(routing);
                        rabbitAdmin.declareBinding(binding);
                        log.debug("Declared binding for queue {} with routing key {}", rabbitQueue.getName(), routing);
                    } catch (Exception e) {
                        log.warn("Failed to bind queue {} with action {}", rabbitQueue.getName(), action, e);
                    }
                }
                try {
                    SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
                    endpoint.setMessageListener(adapter);
                    endpoint.setMessageConverter(messageConverter);
                    endpoint.setQueues(rabbitQueue);
                    endpoint.setAutoStartup(true);
                    endpoints.add(endpoint);
                } catch (Exception e) {
                    log.warn("Failed to start message listener container for domain: {}", domain.getName(), e);
                }

            } catch (Exception e) {
                log.warn("Unexpected error during processing domain '{}': {}", domain.getName(), e.getMessage(), e);
            }
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
                    if (action.isBlank()) {
                        log.warn("Resolved action is blank for method {} in class {}", classMethod.getName(), beanClass.getName());
                        continue;
                    }
                    classMethod.setAccessible(true);
                    var actionType = iEventActionContext.getByName(action);
                    if (actionType == null) {
                        log.warn("Action type '{}' not found for method {}. Skipping.", action, classMethod.getName());
                        continue;
                    }
                    String cancelName = actionAnnotated.cancelMethod();
                    Method cancelMethod = null;

                    for (Method cancelCandidate : beanClass.getDeclaredMethods()) {
                        if (cancelName != null && !cancelName.isBlank() && cancelCandidate.getName().equals(cancelName)) {
                            cancelMethod = cancelCandidate;
                            break;

                        }
                    }
                    if (cancelMethod != null) {
                        log.debug("Registering cancelable handler for action '{}' with cancel method '{}'", action, cancelMethod.getName());
                        var cancelActionType = iEventActionContext.getOrCreateCancelByAction(actionType);
                        handlerContext.addHandler(domain, cancelActionType, new CancelableMessageHandlerAdapter(classMethod, bean, iEventActionContext, rabbitHandlerArgumentResolverComposite));
                    }
                    log.debug("Registering handler for action '{}'", action);
                    handlerContext.addHandler(domain, actionType, new CancelableMessageHandlerAdapter(classMethod, bean, iEventActionContext, rabbitHandlerArgumentResolverComposite));
                }
            }
        }
    }

    private Queue initQueue(org.springframework.amqp.rabbit.annotation.Queue queueAnnotation, IMessagingDomain domain) {
        Queue rabbitQueue = null;
        try {
            rabbitQueue = queueAnnotationParser.parseQueue(queueAnnotation);
            rabbitAdmin.declareQueue(rabbitQueue);
            log.debug("Declared queue: {}", rabbitQueue.getName());
        } catch (Exception e) {
            log.warn("Failed to parse or declare queue for annotation: {}", queueAnnotation, e);
        }
        if (rabbitQueue == null) {
            rabbitQueue = new Queue(queueStrategy.createQueueNameForDomain(domain), true);
        }
        return rabbitQueue;
    }


}


