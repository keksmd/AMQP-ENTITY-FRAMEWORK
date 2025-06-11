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
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;
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
    private final List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    @Override
    public void start() {
        IMessagingDomain cancelDomain = domainContext.getByName(CancelPayload.CANCEL_DOMAIN);
        for (var domain : domainContext.getAllDomains()) {
            if (!CancelPayload.CANCEL_DOMAIN.equals(domain.getName())) {
                var exchange = exchangeContext.getExchange(domain);
                var queues = queueContext.getQueueListByDomain(domain);
                List<Queue> queuesToListen = new ArrayList<>();
                var actions = iEventActionContext.getAllowedActionsByDomian(domain);

                for (var queueAnnotation : queues) {
                    var rabbitQueue = queueAnnotationParser.parseQueue(queueAnnotation);
                    rabbitAdmin.declareQueue(rabbitQueue);
                    if (!domain.isCreateDefaultBindings()) {
                        actions = actions.stream()
                                .filter(a -> !(a instanceof CRUDEventActionTypes ||
                                               a instanceof CancelEventActionTemplate cancel &&
                                               cancel.getActionToCancel() instanceof CRUDEventActionTypes))
                                .toList();
                    }

                    for (var action : actions) {
                        var routing = routingKeyConverter.toRoutingKey(domain, action);
                        var binding = (action instanceof CancelEventActionTemplate)
                                ? BindingBuilder.bind(rabbitQueue).to(exchangeContext.getExchange(cancelDomain)).with(routing)
                                : BindingBuilder.bind(rabbitQueue).to(exchange).with(routing);
                        rabbitAdmin.declareBinding(binding);
                    }

                    queuesToListen.add(rabbitQueue);
                }

                if (!queuesToListen.isEmpty()) {
                    var container = new SimpleMessageListenerContainer();
                    container.setConnectionFactory(connectionFactory);
                    container.setConcurrentConsumers(concurrentConsumers);
                    container.setMaxConcurrentConsumers(maxConcurrentConsumers);
                    container.setQueues(queuesToListen.toArray(new Queue[0]));

                    var delegate = new UniversalMessageListener(internalMessageHandler, objectMapper);
                    var adapter = new MessageListenerAdapter(delegate, "handleMessage") {
                        @Override
                        protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
                            return new Object[]{ extractedMessage, message };
                        }
                    };
                    adapter.setMessageConverter(messageConverter);
                    container.setMessageListener(adapter);
                    container.setAutoStartup(true);
                    container.start();

                    containers.add(container);
                }
            }
        }
    }

    @Override
    public void stop() {
        containers.forEach(SimpleMessageListenerContainer::stop);
    }

    @Override
    public boolean isRunning() {
        return containers.stream().anyMatch(SimpleMessageListenerContainer::isRunning);
    }
}




