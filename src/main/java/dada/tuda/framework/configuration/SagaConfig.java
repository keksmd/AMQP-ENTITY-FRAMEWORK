package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.MessagingRepositoriesMissingAnnotationChecker;
import dada.tuda.framework.consistency.MessageRepository;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.RedisCachingIdempotencyProvider;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.SimpleDomain;
import dada.tuda.framework.crud.contexts.CancelEventActionContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.EventActionContextWithCancel;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.QueueNameContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import dada.tuda.framework.normalization.types.interfaces.EntityProducer;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

@Configuration
@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class SagaConfig {
    @Bean
    @Primary
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    MessageStorage eventStorager(MessageRepository repo, MessageMapper mapper) {
        return new RedisCachingIdempotencyProvider(repo, mapper);
    }


    @Bean
    @ConditionalOnClass(AutoConfigurationPackages.class)
    @ConditionalOnBean({ ConnectionFactory.class })
    MessagingRepositoriesMissingAnnotationChecker checker() {
        return new MessagingRepositoriesMissingAnnotationChecker();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    SimpleDomain cancelDomain() {
        return new SimpleDomain("cancel");
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    @ConditionalOnBean(value = ConnectionFactory.class, name = "cancelDomain")
    IEventActionContext eventActionContext(ConnectionFactory connectionFactory, List<IEventAction> values, QueueNameContext queueNameContext, @Qualifier(value = "cancelDomain") SimpleDomain cancelDomain, ExchangeContext exchangeContext, RoutingKeyConverter routingKeyConverter) {
        return new EventActionContextWithCancel(connectionFactory, values, queueNameContext, cancelDomain, exchangeContext, routingKeyConverter);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    public MessageCanceller eventCanceler(MessageSender sender, CancelEventActionContext actionContext, EntityContext entityContext, DescriptorConverter descriptorConverter, MessageStorage messageStorage) {
        return new MessageCanceller(new EntityProducer<>(sender, entityContext, descriptorConverter, messageStorage), actionContext);
    }

    @Bean
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    MapToJsonConverter mapToJsonConverter(ObjectMapper objectMapper) {
        return new MapToJsonConverter(objectMapper);
    }


}
