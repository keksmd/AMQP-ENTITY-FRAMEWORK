package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.InMemoryIdempotencyProvider;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.consistency.mapper.MessageMapperImpl;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.MessagingEntitesByAnnotationRegistrar;
import dada.tuda.framework.crud.QueueAnnotationParser;
import dada.tuda.framework.crud.contexts.AnnotationDomainContext;
import dada.tuda.framework.crud.contexts.AnnotationEntityContext;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.EventActionContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.MapStoragingQueueAnnotationContext;
import dada.tuda.framework.crud.contexts.PerServiceQueueStrategy;
import dada.tuda.framework.crud.contexts.QueueAnnotationContext;
import dada.tuda.framework.crud.contexts.QueueStrategy;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.extractor.TypeRoutingKeyConverter;
import dada.tuda.framework.crud.extractor.UUUDOperationIdGenerator;
import dada.tuda.framework.crud.listening.MessagingContainerAutoRegistrar;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.handling.DomainHandlersByAnnotationRegistrar;
import dada.tuda.framework.handling.ExchangesByDomainCreator;
import dada.tuda.framework.handling.InternalMessageHandler;
import dada.tuda.framework.normalization.Header;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.types.interfaces.EntityProducer;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Slf4j
@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class MessagingConfiguration {
    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public SmartInitializingSingleton ex(DomainContext domainContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory) {
        return new ExchangesByDomainCreator(connectionFactory, domainContext, exchangeContext);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    BeanFactoryPostProcessor domainHandlersByAnnotationRegistrar(DomainContext domainContext,
                                                                 IEventActionContext eventActionContext,
                                                                 HandlerContext handlerContext,
                                                                 ObjectMapper objectMapper) {
        return new DomainHandlersByAnnotationRegistrar(domainContext, eventActionContext, handlerContext, objectMapper);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    ExchangeContext exchangeProvider(List<TopicExchange> topics) {
        return new ExchangeContext(topics);
    }

    @Bean(initMethod = "init")
    @ConditionalOnBean(ConnectionFactory.class)
    public IEventActionContext iEventActionContext(List<IEventAction> actions, DomainContext domainContext) {
        return new EventActionContext(actions, domainContext);
    }

    @Bean(initMethod = "init")
    @ConditionalOnBean(ConnectionFactory.class)
    public InternalMessageHandler messageHandlerRegistry(ObjectProvider<DadaTudaFrameworkProperties> properties, IEventActionContext actionContext, MessageMapper mapper, @Autowired(required = false) MessageCanceller messageCanceller, HandlerContext handlerContext, MessageStorage messageStorage) {

        return new InternalMessageHandler(messageStorage, messageCanceller, mapper, actionContext, handlerContext, properties);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    HandlerContext handlerContext() {
        return new HandlerContext();
    }


    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessagingContainerAutoRegistrar messagingContainerAutoRegistrar(@Value("${spring.rabbitmq.listener.simple.concurrency:3}") Integer consumers, QueueAnnotationParser annotationParser, @Value("${spring.rabbitmq.listener.simple.max-concurrency:10}") Integer maxConsumers, MessageConverter converter, IEventActionContext iEventActionContext, QueueAnnotationContext queueContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory, DomainContext domainContext, RoutingKeyConverter routingKeyConverter, InternalMessageHandler internalMessageHandler, ObjectMapper objectMapper) {
        return new MessagingContainerAutoRegistrar(domainContext, queueContext, exchangeContext, new RabbitAdmin(connectionFactory), iEventActionContext, routingKeyConverter, connectionFactory, internalMessageHandler, annotationParser, objectMapper,
                consumers, maxConsumers, converter);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueAnnotationParser annotationParser(Environment environment) {
        return new QueueAnnotationParser(environment);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueAnnotationContext queueContext() {
        return new MapStoragingQueueAnnotationContext();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueStrategy queueStrategy(DadaTudaFrameworkProperties properties) {
        return new PerServiceQueueStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean({ MessageStorage.class })
    @ConditionalOnBean(ConnectionFactory.class)
    MessageStorage inMemory(MessageMapper messageMapper) {
        return new InMemoryIdempotencyProvider(messageMapper);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public EntityProducer<?> entityProducer(MessageSender messageSender, EntityContext entityContext, DescriptorConverter descriptorConverter, MessageStorage messageStorage) {
        return new EntityProducer<>(messageSender, entityContext, descriptorConverter, messageStorage);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public OperationIdGenerator operationIdGenerator() {
        return new UUUDOperationIdGenerator();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public DescriptorConverter descriptorConverter(ObjectMapper objectMapper, OperationIdGenerator operationIdGenerator, MessageMapper mapper) {
        return new DescriptorConverter(objectMapper, operationIdGenerator);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageSender eventSender(ExchangeContext exchangeContext, MessageMapper mapper, RabbitTemplate rabbitTemplate, RoutingKeyConverter routingKeyConverter, HeadersGenerator headersGenerator, ObjectMapper objectMapper) {
        return new MessageSender(rabbitTemplate, exchangeContext, objectMapper, mapper, headersGenerator, routingKeyConverter);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    EntityContext entityContext() {
        return new AnnotationEntityContext();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RoutingKeyConverter routingKeyExtractor(DomainContext domains) {
        return new TypeRoutingKeyConverter(domains);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    HeadersGenerator headersGenerator(@Autowired List<Header> headers) {
        return new HeadersGenerator(headers);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitTemplateCustomizer rabbitTemplateCustomizer(Jackson2JsonMessageConverter converter) {
        return rabbitTemplate -> rabbitTemplate.setMessageConverter(converter);
    }


    @Bean
    @ConditionalOnBean({ ObjectMapper.class, ConnectionFactory.class })
    public Jackson2JsonMessageConverter jsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @ConditionalOnBean(ConnectionFactory.class)
    @Bean
    public BeanDefinitionRegistryPostProcessor messagingEntitesByAnnotationRegistrar(EntityContext context, DomainContext domainContext, QueueAnnotationContext queueContext) {
        return new MessagingEntitesByAnnotationRegistrar(context, domainContext, queueContext);
    }

    @Bean()
    @ConditionalOnBean(ConnectionFactory.class)
    AnnotationDomainContext domainContext() {
        return new AnnotationDomainContext();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageMapper messageMapper(DomainContext domainContext, IEventActionContext eventActionContext) {
        MessageMapperImpl impl = new MessageMapperImpl();
        impl.domainContext = domainContext;
        impl.eventActionContext = eventActionContext;
        return impl;
    }


    @Configuration
    @ComponentScan(basePackages = "dada.tuda.framework.normalization.types.realizations")
    public static class TypesRealizationConfig {
    }

}
