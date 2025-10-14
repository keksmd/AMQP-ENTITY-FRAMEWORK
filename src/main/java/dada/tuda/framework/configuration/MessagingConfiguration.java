package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.InMemoryIdempotencyProvider;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.consistency.mapper.MessageMapperImpl;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.DynamicDeclarableRegistrar;
import dada.tuda.framework.crud.MessagingEntitesByAnnotationRegistrar;
import dada.tuda.framework.crud.QueueAnnotationParser;
import dada.tuda.framework.crud.contexts.AnnotationDomainContext;
import dada.tuda.framework.crud.contexts.AnnotationEntityContext;
import dada.tuda.framework.crud.contexts.BindingContext;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.EventActionContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.HandlerContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.contexts.MapBindingContext;
import dada.tuda.framework.crud.contexts.MapStoragingQueueContext;
import dada.tuda.framework.crud.contexts.PerServiceQueueStrategy;
import dada.tuda.framework.crud.contexts.QueueContext;
import dada.tuda.framework.crud.contexts.QueueStrategy;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.extractor.TypeRoutingKeyConverter;
import dada.tuda.framework.crud.extractor.UUUDOperationIdGenerator;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.handling.DomainHandlerInitializer;
import dada.tuda.framework.handling.ExchangesByDomainCreator;
import dada.tuda.framework.handling.InternalMessageHandler;
import dada.tuda.framework.handling.conversion.RabbitHandlerArgumentResolverComposite;
import dada.tuda.framework.handling.conversion.resolvers.ActorIdArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.NormalMessageArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.ObjectIdArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.OperationIdArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.PayloadArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.PayloadMapArgumentResolver;
import dada.tuda.framework.handling.conversion.resolvers.RabbitHandlerArgumentResolver;
import dada.tuda.framework.normalization.Header;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.types.interfaces.EntityProducer;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

@Slf4j
@ImportAutoConfiguration({ EnumBeanConfiguration.class, PropsConfig.class })

@AutoConfiguration(after = { RabbitAutoConfiguration.class, JacksonAutoConfiguration.class })
public class MessagingConfiguration {
    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public SmartInitializingSingleton ex(DomainContext domainContext, ExchangeContext exchangeContext, ConnectionFactory connectionFactory) {
        return new ExchangesByDomainCreator(connectionFactory, domainContext, exchangeContext);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public ObjectMapper objectMapperForRabbitEntities() {
        ObjectMapper mapper = new ObjectMapper();
        var module = new JavaTimeModule();
        mapper = mapper.registerModule(module);
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper = mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper = mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        return mapper;
    }

    @Bean
    @DependsOn("objectMapperForRabbitEntities")
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitListenerConfigurer domainHandlersByAnnotationRegistrar(DomainContext domainContext,
                                                                 IEventActionContext eventActionContext,
                                                                 HandlerContext handlerContext,
                                                                 ApplicationContext applicationContext,
                                                                 RabbitHandlerArgumentResolverComposite rabbitHandlerArgumentResolverComposite,
                                                                 MessageConverter converter,
                                                                 ExchangeContext exchangeContext,
                                                                 RoutingKeyConverter routingKeyConverter,
                                                                 InternalMessageHandler internalMessageHandler,
                                                                 @Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper,
                                                                 @Value("${dada.tuda.framework.messaging.decompose-routing-key}") boolean decompose, QueueContext queueContext, BindingContext bindingContext) {

        return new DomainHandlerInitializer(domainContext, applicationContext, handlerContext, eventActionContext, rabbitHandlerArgumentResolverComposite, queueContext, exchangeContext, routingKeyConverter, internalMessageHandler, objectMapper, converter, decompose, bindingContext);
    }


    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver payloadArgumentResolver(PayloadConverter payloadConverter) {
        return new PayloadArgumentResolver(payloadConverter);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver payloadMapArgumentResolver() {
        return new PayloadMapArgumentResolver();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver normalMessageArgumentResolver() {
        return new NormalMessageArgumentResolver();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver objectIdArgumentResolver() {
        return new ObjectIdArgumentResolver();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver operationIdArgumentResolver() {
        return new OperationIdArgumentResolver();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitHandlerArgumentResolver actorIdArgumentResolver() {
        return new ActorIdArgumentResolver();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @DependsOn("objectMapperForRabbitEntities")
    PayloadConverter payloadConverter(EntityContext entityContext, @Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper) {
        return new PayloadConverter(entityContext, objectMapper);
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

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitHandlerArgumentResolverComposite rabbitHandlerArgumentResolverComposite(List<RabbitHandlerArgumentResolver> resolvers) {
        return new RabbitHandlerArgumentResolverComposite(resolvers);
    }

    @Bean(initMethod = "init")
    @ConditionalOnBean(ConnectionFactory.class)
    public InternalMessageHandler messageHandlerRegistry(ObjectProvider<DadaTudaFrameworkProperties> properties, IEventActionContext eventActionContext, DomainContext domainContext, @Autowired(required = false) MessageCanceller messageCanceller, HandlerContext handlerContext, MessageStorage messageStorage) {

        return new InternalMessageHandler(messageStorage, messageCanceller, eventActionContext, handlerContext, domainContext, properties);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    HandlerContext handlerContext() {
        return new HandlerContext();
    }


    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueAnnotationParser annotationParser(Environment environment) {
        return new QueueAnnotationParser(environment);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueContext queueContext() {
        return new MapStoragingQueueContext();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    QueueStrategy queueStrategy() {
        return new PerServiceQueueStrategy();
    }

    @Bean(initMethod = "init")
    @ConditionalOnMissingBean({ MessageStorage.class })
    @ConditionalOnBean(ConnectionFactory.class)
    MessageStorage inMemory(@Value("${dada.tuda.framework.messaging.cache.inMemory.size:100}") Integer size) {
        return new InMemoryIdempotencyProvider(size);
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
    @DependsOn("objectMapperForRabbitEntities")
    public DescriptorConverter descriptorConverter(@Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper, OperationIdGenerator operationIdGenerator) {
        return new DescriptorConverter(objectMapper, operationIdGenerator);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @DependsOn("objectMapperForRabbitEntities")
    public MessageSender eventSender(ExchangeContext exchangeContext, MessageMapper mapper, RabbitTemplate rabbitTemplate, IEventActionContext eventActionContext, DomainContext domainContext, PayloadConverter payloadConverter, RoutingKeyConverter routingKeyConverter, HeadersGenerator headersGenerator, @Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper) {
        return new MessageSender(rabbitTemplate, exchangeContext, objectMapper, mapper, domainContext, eventActionContext, headersGenerator, payloadConverter, routingKeyConverter);
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
    @ConditionalOnBean({ ConnectionFactory.class })
    @DependsOn("objectMapperForRabbitEntities")
    public Jackson2JsonMessageConverter jsonMessageConverter(@Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @ConditionalOnBean(ConnectionFactory.class)
    @Bean
    public BeanDefinitionRegistryPostProcessor messagingEntitesByAnnotationRegistrar(EntityContext context, QueueAnnotationParser queueAnnotationParser, DomainContext domainContext, QueueContext queueContext, QueueStrategy queueStrategy) {
        return new MessagingEntitesByAnnotationRegistrar<>(context, domainContext, queueContext, queueAnnotationParser, queueStrategy);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public SmartInitializingSingleton dynamicDeclarables(ConnectionFactory connectionFactory, GenericApplicationContext applicationContext, BindingContext bindingContext, QueueContext queueContext, ExchangeContext exchangeContext) {
        return new DynamicDeclarableRegistrar(applicationContext, new RabbitAdmin(connectionFactory), bindingContext, exchangeContext, queueContext);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    BindingContext bindingContext() {
        return new MapBindingContext();
    }

    @Bean()
    AnnotationDomainContext domainContext() {
        return new AnnotationDomainContext();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageMapper messageMapper() {
        return new MessageMapperImpl();
    }


    @Configuration
    @Import({ CancelPayload.class, CRUDEventActionTypes.class })
    public static class TypesRealizationConfig {
    }

}
