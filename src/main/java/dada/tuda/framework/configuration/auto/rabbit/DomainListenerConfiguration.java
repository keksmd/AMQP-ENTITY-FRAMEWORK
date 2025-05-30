package dada.tuda.framework.configuration.auto.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.configuration.auto.DadaTudaFrameworkProperties;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.IEventActionContextImpl;
import dada.tuda.framework.crud.contexts.MapStoragingQueueNameContext;
import dada.tuda.framework.crud.contexts.PerServiceQueueStrategy;
import dada.tuda.framework.crud.contexts.QueueNameContext;
import dada.tuda.framework.crud.contexts.QueueStrategy;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.listening.MessagingContainerAutoRegistrar;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DomainListenerConfiguration {

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessagingContainerAutoRegistrar messagingContainerAutoRegistrar(AmqpAdmin amqpAdmin, IEventActionContext iEventActionContext, QueueNameContext queueContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory, DomainContext domainContext, RoutingKeyConverter routingKeyConverter, MessageHandlerRegistry messageHandlerRegistry, ObjectMapper objectMapper) {
        return new MessagingContainerAutoRegistrar(queueContext, exchangeContext, connectionFactory, domainContext, routingKeyConverter, messageHandlerRegistry, objectMapper, iEventActionContext, amqpAdmin);
    }


    @Bean
    IEventActionContext iEventActionContext(List<IEventAction> actions) {
        return new IEventActionContextImpl(actions);
    }

    @Bean
    QueueNameContext queueContext() {
        return new MapStoragingQueueNameContext();
    }

    @Bean
    QueueStrategy queueStrategy(DadaTudaFrameworkProperties properties) {
        return new PerServiceQueueStrategy(properties);
    }
}
