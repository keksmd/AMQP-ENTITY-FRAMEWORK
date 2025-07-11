package dada.tuda.framework.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class MessageSender {
    private final RabbitTemplate rabbitTemplate;
    private final ExchangeContext exchangeContext;
    private final ObjectMapper objectMapper;
    private final MessageMapper mapper;
    private final DomainContext domainContext;
    private final IEventActionContext eventActionContext;
    private final HeadersGenerator headersGenerator;
    private final PayloadConverter payloadConverter;
    private final RoutingKeyConverter routingKeyConverter;

    public void sendUsingType(NormalMessage event) {
        TopicExchange exchange = this.exchangeContext.getExchange(domainContext.getByName(event.getDomainName()));
        String routingKey = toRoutingKey(event);
        this.rabbitTemplate.convertAndSend(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
    }

    private String toRoutingKey(NormalMessage event) {
        IMessagingDomain domain = domainContext.getByName(event.getDomainName());
        IEventAction action = eventActionContext.getByName(event.getActionTypeName());
        return routingKeyConverter.toRoutingKey(domain, action);
    }

    public void sendUsingTypeWithExchangeForOtherDomain(NormalMessage event, IMessagingDomain domain) {
        TopicExchange exchange = this.exchangeContext.getExchange(domain);
        String routingKey = toRoutingKey(event);
        this.rabbitTemplate.convertAndSend(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
    }

    public <T> T sendRequestUsingType(NormalMessage event, Class<T> responseType) throws TimeoutException {
        TopicExchange exchange = this.exchangeContext.getExchange(domainContext.getByName(event.getDomainName()));
        String routingKey = toRoutingKey(event);
        Object response = this.rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
        if (response == null) {
            throw new TimeoutException("No request response: ");
        } else {
            if (response instanceof NormalMessage normalMessage) {
                return payloadConverter.convertPayload(normalMessage.getPayloadMap(), responseType);
            } else {
                return this.objectMapper.convertValue(response, responseType);
            }

        }
    }

}

