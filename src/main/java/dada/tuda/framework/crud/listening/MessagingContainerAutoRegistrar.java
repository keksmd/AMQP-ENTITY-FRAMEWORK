package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.QueueNameContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import jakarta.validation.constraints.NotNull;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.List;


/**
 * Автоматически регистрирует:
 * 1) Прокси-реализации MessagingEntittyRepository<T> на основе generic-типа
 * 2) Слушатели сообщений для всех доменов
 */

public class MessagingContainerAutoRegistrar implements BeanDefinitionRegistryPostProcessor {
    private final QueueNameContext queueContext;
    private final ExchangeContext exchangeContext;
    private final ConnectionFactory connectionFactory;
    private final DomainContext domainContext;
    private final RoutingKeyConverter routingKeyConverter;
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final ObjectMapper objectMapper;
    private final IEventActionContext iEventActionContext;
    private final RabbitAdmin rabbitAdmin;

    public MessagingContainerAutoRegistrar(QueueNameContext queueContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory, DomainContext domainContext, RoutingKeyConverter routingKeyConverter, MessageHandlerRegistry messageHandlerRegistry, ObjectMapper objectMapper, IEventActionContext iEventActionContext) {
        this.queueContext = queueContext;
        this.exchangeContext = exchangeContext;
        this.connectionFactory = connectionFactory;
        this.domainContext = domainContext;
        this.routingKeyConverter = routingKeyConverter;
        this.messageHandlerRegistry = messageHandlerRegistry;
        this.objectMapper = objectMapper;
        this.iEventActionContext = iEventActionContext;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }


    @Override
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
        for (var domain : domainContext.getAllDomains()) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            Exchange exchange = exchangeContext.getExchange(domain);
            List<String> queueNames = queueContext.getQueueNameListByDomain(domain);
            List<Queue> queues = queueNames.stream().map(name -> new Queue(name, true)).toList();

            queues.forEach(q -> {
                registerBeanIfAbsent(registry, q.getName() + "Bean", q);
                rabbitAdmin.declareQueue(q);
                for (IEventAction action : iEventActionContext.getAllowedActionsByDomian(domain)) {
                    String routing = routingKeyConverter.toRoutingKey(domain, action);
                    Binding binding = BindingBuilder.bind(q).to(exchange).with(routing).noargs();
                    registerBeanIfAbsent(registry, domain.getName() + q.getName() + "Binding", binding);
                    rabbitAdmin.declareBinding(binding);
                }
            });

            container.setQueues(queues.toArray(new Queue[0]));
            container.setMessageListener(new UniversalMessageListener(messageHandlerRegistry, objectMapper));
            container.setAutoStartup(true);
            container.start();

            // Регистрируем контейнер
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class, () -> container);
            registry.registerBeanDefinition(domain.getName().toLowerCase() + "Container", builder.getBeanDefinition());
        }
    }

    private <T> void registerBeanIfAbsent(BeanDefinitionRegistry registry, String name, Object instance) {
        if (!registry.containsBeanDefinition(name)) {
            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition((Class<T>) instance.getClass(), () -> (T) instance);
            registry.registerBeanDefinition(name, bdb.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.@NotNull ConfigurableListableBeanFactory beanFactory) {
    }
}




