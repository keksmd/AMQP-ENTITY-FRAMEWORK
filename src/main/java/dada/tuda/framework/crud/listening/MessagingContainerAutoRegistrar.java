package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import dada.tuda.framework.crud.QueueAnnotationParser;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.QueueAnnotationContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.handling.InternalMessageHandler;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.SmartLifecycle;

import java.util.ArrayList;
import java.util.List;


/**
 * Автоматически регистрирует:
 * 1) Прокси-реализации MessagingEntittyRepository<T> на основе generic-типа
 * 2) Слушатели сообщений для всех доменов
 */
@RequiredArgsConstructor
@Slf4j
public class MessagingContainerAutoRegistrar implements SmartLifecycle {
    private final DomainContext domainContext;
    private final QueueAnnotationContext queueContext;
    private final ExchangeContext exchangeContext;
    private final RabbitAdmin rabbitAdmin;
    private final IEventActionContext iEventActionContext;
    private final RoutingKeyConverter routingKeyConverter;
    private final ConnectionFactory connectionFactory;
    private final InternalMessageHandler internalMessageHandler;
    private final QueueAnnotationParser queueAnnotationParser;
    private final ObjectMapper objectMapper;
    private final int concurrentConsumers;
    private final int maxConcurrentConsumers;
    private final MessageConverter messageConverter;
    private final boolean decompose;
    private final List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    @Override
    public void start() {
        log.debug("Starting MessagingContainerAutoRegistrar...");
        IMessagingDomain cancelDomain = domainContext.getByName(CancelPayload.CANCEL_DOMAIN);

        for (var domain : domainContext.getAllDomains()) {
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
            var queues = queueContext.getQueueListByDomain(domain);
            if (queues == null || queues.isEmpty()) {
                log.warn("No queues found for domain: {}", domain.getName());
                continue;
            }
            var actions = iEventActionContext.getAllowedActionsByDomian(domain);
            if (actions == null || actions.isEmpty()) {
                log.warn("No allowed actions found for domain: {}", domain.getName());
                continue;
            }
            List<Queue> queuesToListen = new ArrayList<>();

            for (var queueAnnotation : queues) {
                Queue rabbitQueue;
                try {
                    rabbitQueue = queueAnnotationParser.parseQueue(queueAnnotation);
                    rabbitAdmin.declareQueue(rabbitQueue);
                    log.debug("Declared queue: {}", rabbitQueue.getName());
                } catch (Exception e) {
                    log.warn("Failed to parse or declare queue for annotation: {}", queueAnnotation, e);
                    continue;
                }
                var filteredActions = domain.isCreateDefaultBindings() ? actions : actions.stream().filter(a -> !(a instanceof CRUDEventActionTypes || a instanceof CancelEventActionTemplate cancel && cancel.getActionToCancel() instanceof CRUDEventActionTypes)).toList();
                if (filteredActions.isEmpty()) {
                    log.warn("Filtered actions for queue {} in domain {} are empty", rabbitQueue.getName(), domain.getName());
                    continue;
                }
                for (var action : filteredActions) {
                    try {
                        var routing = routingKeyConverter.toRoutingKey(domain, action);
                        var binding = (action instanceof CancelEventActionTemplate) ? BindingBuilder.bind(rabbitQueue).to(exchangeContext.getExchange(cancelDomain)).with(routing) : BindingBuilder.bind(rabbitQueue).to(exchange).with(routing);
                        rabbitAdmin.declareBinding(binding);
                        log.debug("Declared binding for queue {} with routing key {}", rabbitQueue.getName(), routing);
                    } catch (Exception e) {
                        log.warn("Failed to bind queue {} with action {}", rabbitQueue.getName(), action, e);
                    }
                }
                queuesToListen.add(rabbitQueue);
            }
            if (queuesToListen.isEmpty()) {
                log.warn("No queues to listen for domain: {}", domain.getName());
                continue;
            }
            try {
                var container = new SimpleMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.setConcurrentConsumers(concurrentConsumers);
                container.setMaxConcurrentConsumers(maxConcurrentConsumers);
                container.setQueues(queuesToListen.toArray(new Queue[0]));

                var delegate = new UniversalMessageListener(internalMessageHandler, objectMapper);
                var adapter = new MessageListenerAdapter(delegate, "handleMessage") {
                    @Override
                    protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
                        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
                        if (routingKey != null) {
                            if (extractedMessage instanceof NormalMessage msg && (msg.getActionTypeName() == null || msg.getDomainName() == null) && decompose) {
                                if (msg.getActionTypeName() == null) {
                                    msg.setActionTypeName(routingKey.contains(".") ? routingKey.split("\\.")[1] : routingKey);
                                }
                                if (msg.getDomainName() == null) {
                                    msg.setDomainName(domain.getName());
                                }
                            }
                        }
                        return new Object[]{ extractedMessage };
                    }
                };
                adapter.setMessageConverter(messageConverter);
                container.setMessageListener(adapter);
                container.setAutoStartup(true);
                container.start();

                log.debug("Started message listener container for domain: {}", domain.getName());
                containers.add(container);
            } catch (Exception e) {
                log.warn("Failed to start message listener container for domain: {}", domain.getName(), e);
            }
        }

        log.debug("MessagingContainerAutoRegistrar started.");
    }

    @Override
    public void stop() {
        log.debug("Stopping all message listener containers...");
        containers.forEach(container -> {
            try {
                container.stop();
                log.debug("Stopped container: {}", container);
            } catch (Exception e) {
                log.warn("Failed to stop container: {}", container, e);
            }
        });
        log.debug("All message listener containers stopped.");
    }

    @Override
    public boolean isRunning() {
        boolean running = containers.stream().anyMatch(SimpleMessageListenerContainer::isRunning);
        log.debug("MessagingContainerAutoRegistrar running state: {}", running);
        return running;
    }
}




