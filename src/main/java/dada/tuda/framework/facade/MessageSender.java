package dada.tuda.framework.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.ExchangeProvider;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
public class MessageSender {
    private final ExchangeProvider exchangeProvider;
    private final RabbitTemplate rabbitTemplate;
   
    private final HeadersGenerator headersGenerator;
    private final ObjectMapper objectMapper;

    public void sendUsingType(AbstractNormalMessage event) {
       IMessagingEventType type = event.getType();
        TopicExchange exchange = exchangeProvider.getExchange(type.getAggregate());
        String routingKey = type.toRoutingKey();
        rabbitTemplate.convertAndSend(
                exchange.getName(),
                routingKey,
                event,
                message -> {
                    this.headersGenerator.accept(message.getMessageProperties().getHeaders());
                    return message;
                }
        );
    }
    public void sendMessageCancel(AbstractNormalMessage event) {
        IMessagingEventType type = event.getType();
        TopicExchange exchange = exchangeProvider.getExchange(type.getAggregate());
        String routingKey = type.toRoutingKey()+ CancelUtils.CANCELLED_ACTION.name().toLowerCase();
        rabbitTemplate.convertAndSend(
                exchange.getName(),
                routingKey,
                event,
                message -> {
                    this.headersGenerator.accept(message.getMessageProperties().getHeaders());
                    return message;
                }
        );
    }
    public  <T> T sendRequestUsingType(AbstractNormalMessage event, TypeReference<T> returning) {
        IMessagingEventType type = event.getType();
        TopicExchange exchange = exchangeProvider.getExchange(type.getAggregate());
        String routingKey = type.toRoutingKey();
        Object response = rabbitTemplate.convertSendAndReceive(
                exchange.getName(),
                routingKey,
                event,
                message -> {
                    this.headersGenerator.accept(message.getMessageProperties().getHeaders());
                    return message;
                }
        );
        if (response == null) {
            throw new RuntimeException("No request response: ");
        } else {
            return objectMapper.convertValue(response, returning);
        }
    }

}
