package dada.tuda.framework.handling;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.consistency.mapper.MessageMapperImpl;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.MessagingEntityDescriptor;
import dada.tuda.framework.crud.SimpleDomain;
import dada.tuda.framework.crud.contexts.AnnotationDomainContext;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.crud.contexts.EventActionContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.crud.extractor.TypeRoutingKeyConverter;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.EntityProducer;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntityProducerTest {

    SimpleDomain d;
    TopicExchange t;
    TestEntity entity = new TestEntity();
    ObjectMapper objectMapper;
    MessagingEntityDescriptor descriptor;
    private RabbitTemplate rabbitTemplate;
    private ExchangeContext exchangeContext;
    private RoutingKeyConverter routingKeyConverter;
    private EntityContext entityContext;
    private DescriptorConverter descriptorConverter;
    private EntityProducer<TestEntity> entityProducer;
    private MessageMapper mapper;
    private MessageStorage messageStorage;

    @BeforeEach
    void setUp() {
        d = new SimpleDomain("test");
        descriptor = new MessagingEntityDescriptor();
        descriptor.setDomain(d);
        entityContext = mock(EntityContext.class);
        when(entityContext.getDescriptorByMessagingEntityClass(TestEntity.class)).thenReturn(descriptor);

        t = new TopicExchange("test-exchange");
        exchangeContext = mock(ExchangeContext.class);
        when(exchangeContext.getExchange(eq(d))).thenReturn(t);
        rabbitTemplate = mock(RabbitTemplate.class);
        objectMapper = new ObjectMapper();
        HeadersGenerator headersGenerator = mock(HeadersGenerator.class);
        DomainContext domainContext = new AnnotationDomainContext();
        domainContext.registerDomain(d);
        routingKeyConverter = new TypeRoutingKeyConverter(domainContext);
        IEventActionContext eventActionContext = new EventActionContext(Arrays.stream(CRUDEventActionTypes.values()).map(c -> (IEventAction) c).toList(), domainContext);
        mapper = new MessageMapperImpl();
        mapper.domainContext = domainContext;
        mapper.domainContext.init();
        mapper.eventActionContext = eventActionContext;

        descriptorConverter = new DescriptorConverter(objectMapper, () -> "opID");

        MessageSender messageSender = new MessageSender(rabbitTemplate, exchangeContext, objectMapper, mapper, domainContext, eventActionContext, headersGenerator, new PayloadConverter(entityContext, new ObjectMapper()), routingKeyConverter);
        entityProducer = new EntityProducer<>(messageSender, entityContext, descriptorConverter, messageStorage);
    }

    @Test
    void testPersist() {
        NormalMessage message = new JsonNormalMessage();
        message.setOperationId("opID");
        message.setActionTypeName(CRUDEventActionTypes.CREATED.getName());
        message.setDomainName(d.getName());
        message.setPayloadMap(objectMapper.convertValue(entity, Map.class));


        entityProducer.create(entity);

        verify(rabbitTemplate).convertAndSend(eq(t.getName()), eq("test.created"), eq(message), any(MessagePostProcessor.class));
    }

    @Test
    void testDelete() {

        NormalMessage expected = new JsonNormalMessage();
        expected.setOperationId("opID");
        expected.setActionTypeName(CRUDEventActionTypes.DELETED.getName());
        expected.setPayloadMap(objectMapper.convertValue(entity, Map.class));
        expected.setDomainName(d.getName());


        entityProducer.delete(entity);

        verify(rabbitTemplate).convertAndSend(eq(t.getName()), eq("test.deleted"),
                eq(expected),
                any(MessagePostProcessor.class));
    }

}
