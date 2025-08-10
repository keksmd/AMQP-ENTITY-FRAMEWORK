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
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class DomainHandlerInitializer implements RabbitListenerConfigurer {
    private final List<RabbitListenerEndpoint> endpoints = new ArrayList<>();
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
     * Message converter used by listener adapter.
     */
    private final MessageConverter messageConverter;
    /**
     * Flag indicating whether to decompose routing key into domain and action when not provided.
     */
    private final boolean decompose;
    private MessageListenerAdapter adapter;

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        for (IMessagingDomain iMessagingDomain : domainContext.getAllDomains()) {
            String exchangeName = iMessagingDomain.getExchangeName();
            TopicExchange exchange = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
            exchangeContext.registerExchange(exchange);
            rabbitAdmin.declareExchange(exchange);
        }
        afterSingletonsInstantiated();
        endpoints.forEach(registrar::registerEndpoint);
    }

    /**
     * Scans the application context for beans annotated with {@link DomainHandlers} and registers
     * their handler methods.
     */
    public void afterSingletonsInstantiated() {
        init();

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
            registerHandlersAndEndpointForDomain(domain, beanClass, bean);
        }
    }

    public void init() {
        UniversalMessageListener delegate = new UniversalMessageListener(internalMessageHandler, objectMapper);
        adapter = new MessageListenerAdapter(delegate, "handleMessage") {
            @Override
            protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
                String routingKey = message.getMessageProperties().getReceivedRoutingKey();
                if ((routingKey != null) && (extractedMessage instanceof NormalMessage msg && (msg.getActionTypeName() == null || msg.getDomainName() == null) && decompose) && (msg.getActionTypeName() == null || msg.getDomainName() == null)) {
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
    }

    void registerHandlersAndEndpointForDomain(IMessagingDomain domain, Class<?> beanClass, Object bean) {
        try {
            log.debug("Processing domain: {}", domain.getName());

            if (CancelPayload.CANCEL_DOMAIN.equals(domain.getName())) {
                log.debug("Skipping cancel domain: {}", domain.getName());
                return;
            }
            var exchange = exchangeContext.getExchange(domain);
            if (exchange == null) {
                log.warn("No exchange found for domain: {}", domain.getName());
                return;
            }
            var queueAnnotation = queueContext.getQueueByDomain(domain);
            if (queueAnnotation == null) {
                log.warn("No queue found for domain: {}", domain.getName());
                return;
            }
            log.debug("Found {} queue for domain: {}", queueAnnotation.name(), domain.getName());

            for (Method classMethod : beanClass.getDeclaredMethods()) {
                if (!classMethod.isAnnotationPresent(ActionHandler.class)) {
                    continue;
                }
                ActionHandler actionAnnotated = classMethod.getAnnotation(ActionHandler.class);
                handleActionHandlerMethod(domain, beanClass, bean, classMethod, actionAnnotated);

            }
            Set<IEventAction> withHandler = handlerContext.getActionsWithHandler(domain);

            if (withHandler.isEmpty()) {
                log.warn("No actions with handlers found for domain: {}", domain.getName());
                return;
            }
            Queue rabbitQueue = initQueue(queueAnnotation, domain);
            initActions(withHandler, domain, rabbitQueue, exchange);
            initEndpoint(domain, rabbitQueue);
        } catch (Exception e) {
            log.warn("Unexpected error during processing domain '{}': {}", domain.getName(), e.getMessage(), e);
        }

    }

    private void handleActionHandlerMethod(IMessagingDomain domain, Class<?> beanClass, Object bean, Method classMethod, ActionHandler actionAnnotated) {
        String[] actions = actionAnnotated.action();
        if (actions == null || actions.length == 0) {
            log.warn("No actions defined in @ActionHandler on method {}. Skipping.", classMethod.getName());
            return;
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
                handlerContext.addHandler(domain, cancelActionType, new CancelableMessageHandlerAdapter(cancelMethod, bean, iEventActionContext, rabbitHandlerArgumentResolverComposite));
            }
            log.debug("Registering handler for action '{}'", action);
            handlerContext.addHandler(domain, actionType, new CancelableMessageHandlerAdapter(classMethod, bean, iEventActionContext, rabbitHandlerArgumentResolverComposite));
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

    private void initActions(Collection<IEventAction> actions, IMessagingDomain domain, Queue rabbitQueue, TopicExchange exchange) {
        for (var action : actions) {
            try {
                var routing = routingKeyConverter.toRoutingKey(domain, action);
                Binding binding = BindingBuilder.bind(rabbitQueue).to(exchange).with(routing);
                rabbitAdmin.declareBinding(binding);
                log.debug("Declared binding for queue {} with routing key {}", rabbitQueue.getName(), routing);
            } catch (Exception e) {
                log.warn("Failed to bind queue {} with action {}", rabbitQueue.getName(), action, e);
            }
        }
    }

    private void initEndpoint(IMessagingDomain domain, Queue rabbitQueue) {
        try {
            var endpoint = new SimpleRabbitListenerEndpoint();
            endpoint.setId(domain.getName() + "-" + rabbitQueue.getName() + "-listener");
            endpoint.setMessageListener(adapter);
            endpoint.setMessageConverter(messageConverter);
            endpoint.setQueues(rabbitQueue);
            endpoint.setAutoStartup(true);
            this.registerEndpoint(endpoint);
        } catch (Exception e) {
            log.warn("Failed to start message listener container for domain: {}", domain.getName(), e);
        }
    }

    public void registerEndpoint(RabbitListenerEndpoint endpoint) {
        endpoints.add(endpoint);
    }


}


