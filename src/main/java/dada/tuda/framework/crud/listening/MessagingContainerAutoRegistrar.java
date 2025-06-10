package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.ListenableQueue;
import dada.tuda.framework.crud.QueueAnnotationParser;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.QueueAnnotationContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.ArrayList;
import java.util.List;


/**
 * Автоматически регистрирует:
 * 1) Прокси-реализации MessagingEntittyRepository<T> на основе generic-типа
 * 2) Слушатели сообщений для всех доменов
 */

public class MessagingContainerAutoRegistrar implements SmartInitializingSingleton {
    private final QueueAnnotationContext queueContext;
    private final ExchangeContext exchangeContext;
    private final ConnectionFactory connectionFactory;
    private final DomainContext domainContext;
    private final RoutingKeyConverter routingKeyConverter;
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final ObjectMapper objectMapper;
    private final IEventActionContext iEventActionContext;
    private final RabbitAdmin rabbitAdmin;
    private final Integer maxConcurrentConsumers;
    private final Integer concurrentConsumers;
    private final QueueAnnotationParser queueAnnotationParser;

    public MessagingContainerAutoRegistrar(QueueAnnotationContext queueContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory, DomainContext domainContext, RoutingKeyConverter routingKeyConverter, MessageHandlerRegistry messageHandlerRegistry, ObjectMapper objectMapper, IEventActionContext iEventActionContext, EntityContext entityContext, Integer maxConcurrentConsumers, Integer concurrentConsumers, QueueAnnotationParser queueAnnotationParser) {
        this.queueContext = queueContext;
        this.exchangeContext = exchangeContext;
        this.connectionFactory = connectionFactory;
        this.domainContext = domainContext;
        this.routingKeyConverter = routingKeyConverter;
        this.messageHandlerRegistry = messageHandlerRegistry;
        this.objectMapper = objectMapper;
        this.iEventActionContext = iEventActionContext;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.maxConcurrentConsumers = maxConcurrentConsumers;
        this.concurrentConsumers = concurrentConsumers;
        this.queueAnnotationParser = queueAnnotationParser;
    }


    @Override
    public void afterSingletonsInstantiated() {
        IMessagingDomain cancelDomain = domainContext.getByName(CancelPayload.CANCEL_DOMAIN);
        for (var domain : domainContext.getAllDomains()) {
            if (!CancelPayload.CANCEL_DOMAIN.equals(domain.getName())) {
                Exchange exchange = exchangeContext.getExchange(domain);
                List<ListenableQueue> queues = queueContext.getQueueListByDomain(domain);
                ArrayList<Queue> queuesToListen = new ArrayList<>();
                queues.forEach(qs -> {
                    var q = queueAnnotationParser.parseQueue(qs.value());
                    rabbitAdmin.declareQueue(q);
                    var actions = iEventActionContext.getAllowedActionsByDomian(domain);
                    if (!domain.isCreateDefaultBindings()) {
                        actions = actions.stream()
                                .filter(a -> !(a instanceof CRUDEventActionTypes))
                                .toList();
                    }
                    for (IEventAction action : actions) {
                        String routing = routingKeyConverter.toRoutingKey(domain, action);
                        Binding binding;
                        if (action instanceof CancelEventActionTemplate) {
                            if (cancelDomain != null) {
                                binding = BindingBuilder.bind(q).to(exchangeContext.getExchange(cancelDomain)).with(routing);
                            } else {
                                throw new IllegalStateException("Cancel domain not exist");
                            }
                        } else {
                            binding = BindingBuilder.bind(q).to(exchange).with(routing).noargs();
                        }
                        rabbitAdmin.declareBinding(binding);
                    }
                    if ("true".equalsIgnoreCase(qs.listen())) {
                        queuesToListen.add(q);
                    }
                });

                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.setConcurrentConsumers(this.concurrentConsumers);
                container.setMaxConcurrentConsumers(this.maxConcurrentConsumers);
                container.setQueues(queuesToListen.toArray(new Queue[0]));
                container.setMessageListener(new UniversalMessageListener(messageHandlerRegistry, objectMapper));
                container.setAutoStartup(true);
                container.start();
            }
        }
    }


}




