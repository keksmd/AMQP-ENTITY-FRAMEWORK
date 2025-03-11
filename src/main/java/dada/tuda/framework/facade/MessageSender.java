package dada.tuda.framework.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.ExchangeProvider;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import dada.tuda.framework.normalization.types.realizations.Requested;
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
        IMessagingDomain aggregate = event.getType().getAggregate();
        TopicExchange exchange = exchangeProvider.getExchange(aggregate);
        String routingKey = aggregate.getKey() + "." + event.getType().name().toLowerCase();
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
        IMessagingDomain aggregate = event.getType().getAggregate();
        TopicExchange exchange = exchangeProvider.getExchange(aggregate);
        String routingKey =aggregate.getKey() + "." + event.getType().name().toLowerCase() + CancelUtils.CANCELLED_ACTION.name();
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
        IMessagingDomain aggregate = event.getType().getAggregate();
        TopicExchange exchange = exchangeProvider.getExchange(aggregate);
        String routingKey = aggregate.getKey() + "." + Requested.getInstance().name().toLowerCase();

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
