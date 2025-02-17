package dada.tuda.framework.configuration.rabbit.publiced;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import dada.tuda.framework.configuration.jackson.JaksonConfiguration;
import dada.tuda.framework.configuration.rabbit.ExchangesConfiguration;
import dada.tuda.framework.consistency.DefaultIdempotencyProvider;
import dada.tuda.framework.consistency.EventStorager;
import dada.tuda.framework.consistency.IdempotencyProvider;
import dada.tuda.framework.facade.FrameworkMessagingApi;
import dada.tuda.framework.handling.MessageHandler;
import dada.tuda.framework.handling.MessageHandlerRegistry;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.FrameworkMessageFactory;
import dada.tuda.framework.normalization.converters.IMessagingEventTypeDeserializer;
import dada.tuda.framework.normalization.types.interfaces.IEventActionType;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;

import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import dada.tuda.framework.normalization.types.realizations.RequestedType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;

import javax.naming.OperationNotSupportedException;
import java.util.List;

import static dada.tuda.framework.normalization.converters.IMessagingEventTypeDeserializer.MESSAGING_EVENT_TYPES;

@Slf4j
@EnableRabbit
@Configuration
@Import({JaksonConfiguration.class, ExchangesConfiguration.class})
public class MessagingConfiguration {


    @Bean
    IMessagingEventType cancellingEvent() {
        return CancelUtils.getCancellingEventType();
    }

    @Bean
    public IEventActionType requestedType() {
        return RequestedType.getInstance();
    }

    @Bean
    public FrameworkMessageFactory frameworkMessageFabric(ObjectMapper objectMapper) {
        return new FrameworkMessageFactory(objectMapper);
    }

    @Bean
    IMessagingEventTypeDeserializer iMessagingEventTypeDeserializer(List<IMessagingEventType> types) {
        MESSAGING_EVENT_TYPES.addAll(types);
        return new IMessagingEventTypeDeserializer();
    }

    @Bean
    public MessageHandlerRegistry messageHandlerRegistry(@Autowired List<MessageHandler> handlers, IdempotencyProvider idempotencyProvider, FrameworkMessagingApi frameworkMessagingApi, EventStorager eventStorager) {
        return new MessageHandlerRegistry(handlers, idempotencyProvider, eventStorager, frameworkMessagingApi);
    }

    @Bean
    EventStorager defaultEventStorager() {
        return new EventStorager() {
            @Override
            public AbstractNormalMessage getByID(String operationId) throws OperationNotSupportedException {
                throw new OperationNotSupportedException("This storager is not supported,add saga support");
            }

            @Override
            public void save(AbstractNormalMessage message) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

    @Bean
    public DefaultIdempotencyProvider defaultIdempotencyProvider() {
        return new DefaultIdempotencyProvider();
    }

    @Bean
    @Profile("!unit-test")
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory, @Autowired Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    /**
     * Страховка от отправки сообщений при unit-тестах
     */
    @Bean
    @Profile("unit-test")
    @Primary
    public RabbitTemplate testRabbitTemplate(@Autowired ConnectionFactory connectionFactory, @Autowired Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory) {
            @Override
            public void doSend(Channel channel, String exchangeArg, String routingKeyArg, Message message, boolean mandatory, @Nullable CorrelationData correlationData) {
                log.warn("testRabbitTemplate did not send message (remove profile \"unit-test\" to enable it): {}", message);
            }
        };
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }


    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return new Jackson2JsonMessageConverter(objectMapper);
    }


}
