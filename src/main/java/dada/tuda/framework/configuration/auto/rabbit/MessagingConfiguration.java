package dada.tuda.framework.configuration.auto.rabbit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.configuration.auto.DadaTudaFrameworkProperties;
import dada.tuda.framework.consistency.InMemoryIdempotencyProvider;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.consistency.mapper.MessageMapperImpl;
import dada.tuda.framework.crud.AutoEntityProducer;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.MessagingEntityBeanFactoryPostProcessor;
import dada.tuda.framework.crud.contexts.AnnotationDomainContext;
import dada.tuda.framework.crud.contexts.AnnotationEntityContext;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.EventActionContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.QueueNameContext;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.extractor.TypeRoutingKeyConverter;
import dada.tuda.framework.crud.extractor.UUUDOperationIdGenerator;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.handling.MessageHandler;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.Header;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.EntityProducer;
import dada.tuda.framework.repositories.cancel.CancelPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@ConditionalOnBean(ConnectionFactory.class)
@Import(TypesRealizationConfig.class)
public class MessagingConfiguration {


    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    EventActionContext eventActionContext(List<IEventAction> actions) {
        return new EventActionContext(actions);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageHandlerRegistry messageHandlerRegistry(DadaTudaFrameworkProperties properties, MessageMapper mapper, List<MessageHandler> handlers, MessageCanceller messageCanceller, MessageStorage messageStorage) {
        log.info("Creating MessageHandlerRegistry: {}", handlers);
        return new MessageHandlerRegistry(messageStorage, messageCanceller, mapper, handlers, properties);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageCanceller eventCanceler(MessageSender sender, EntityContext entityContext, DescriptorConverter descriptorConverter, MessageStorage messageStorage) {
        return new MessageCanceller(new EntityProducer<>(sender, entityContext, descriptorConverter, messageStorage));
    }

    @Bean
    @ConditionalOnMissingBean({ MessageStorage.class })
    MessageStorage inMemory() {
        return new InMemoryIdempotencyProvider();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public EntityProducer<CancelPayload> myCancelMessageEntityEntityProducer(MessageSender messageSender, EntityContext entityContext, DescriptorConverter descriptorConverter, MessageStorage messageStorage) {
        return new EntityProducer<>(messageSender, entityContext, descriptorConverter, messageStorage);
    }

    @Bean
    public OperationIdGenerator operationIdGenerator() {
        return new UUUDOperationIdGenerator();
    }

    @Bean
    public DescriptorConverter descriptorConverter(ObjectMapper objectMapper) {
        return new DescriptorConverter(objectMapper);
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public MessageSender eventSender(ExchangeContext exchangeContext, MessageMapper mapper, RabbitTemplate rabbitTemplate, RoutingKeyConverter routingKeyConverter, HeadersGenerator headersGenerator, ObjectMapper objectMapper) {
        return new MessageSender(rabbitTemplate, exchangeContext, objectMapper, mapper, headersGenerator, routingKeyConverter);
    }

    @Bean
    EntityContext entityContext() {
        return new AnnotationEntityContext();
    }

    @Bean
    RoutingKeyConverter routingKeyExtractor(List<IMessagingDomain> domains) {
        return new TypeRoutingKeyConverter(domains);
    }

    @Bean
    HeadersGenerator headersGenerator(@Autowired List<Header> headers) {
        return new HeadersGenerator(headers);
    }


    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory, @Autowired Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    public Jackson2JsonMessageConverter jsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return new Jackson2JsonMessageConverter(objectMapper);
    }


    @Bean
    public MessagingEntityBeanFactoryPostProcessor postProcessor(EntityContext context, DomainContext domainContext, QueueNameContext queueContext) {
        return new MessagingEntityBeanFactoryPostProcessor(context, domainContext, queueContext);
    }

    @Bean
    DomainContext domainContext(Set<IMessagingDomain> domains) {
        return new AnnotationDomainContext(domains);
    }

    @Bean
    public MessageMapper messageMapper(DomainContext domainContext,
                                       EventActionContext eventActionContext) {
        // Получаем «сырую» реализацию
        MessageMapperImpl impl = new MessageMapperImpl();
        // Внедряем зависимости вручную
        impl.domainContext = domainContext;
        impl.eventActionContext = eventActionContext;
        return impl;
    }

    @Bean
    public AutoEntityProducer autoEntityProducerConfiguration(EntityContext entityContext) {
        return new AutoEntityProducer(entityContext);
    }

}
