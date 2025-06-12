package dada.tuda.framework.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
public class MessageSender {
    private final RabbitTemplate rabbitTemplate;
    private final ExchangeContext exchangeContext;
    private final ObjectMapper objectMapper;
    private final MessageMapper mapper;
    private final HeadersGenerator headersGenerator;
    private final PayloadConverter payloadConverter;
    private final RoutingKeyConverter routingKeyConverter;


    public void sendUsingType(NormalizedMessage event) {
        TopicExchange exchange = this.exchangeContext.getExchange(event.getDomain());
        String routingKey = routingKeyConverter.toRoutingKey(event);
        this.rabbitTemplate.convertAndSend(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
    }

    public void sendUsingTypeWithExchangeForOtherDomain(NormalizedMessage event, IMessagingDomain domain) {
        TopicExchange exchange = this.exchangeContext.getExchange(domain);
        String routingKey = routingKeyConverter.toRoutingKey(event);
        this.rabbitTemplate.convertAndSend(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
    }

    public <T> T sendRequestUsingType(NormalizedMessage event, Class<T> responseType) {
        TopicExchange exchange = this.exchangeContext.getExchange(event.getDomain());
        String routingKey = routingKeyConverter.toRoutingKey(event);
        Object response = this.rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, mapper.toMessageFromNormal(event), (message) -> {
            this.headersGenerator.accept(message.getMessageProperties().getHeaders());
            return message;
        });
        if (response == null) {
            throw new RuntimeException("No request response: ");
        } else {
            if (response instanceof NormalMessage normalMessage) {
                return payloadConverter.convertPayload(normalMessage.getPayloadMap(), responseType);
            } else {
                return this.objectMapper.convertValue(response, responseType);
            }

        }
    }

}

