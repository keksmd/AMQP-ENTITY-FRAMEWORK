package dada.tuda.framework.facade;

import dada.tuda.framework.ExchangeProvider;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.HeadersGenerator;
import dada.tuda.framework.normalization.types.interfaces.IEventActionType;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
public class MessageSender {
    private final ExchangeProvider exchangeProvider;
    private final RabbitTemplate rabbitTemplate;
   
    private final HeadersGenerator headersGenerator;

    public void sendUsingType(AbstractNormalMessage event, IEventActionType eventActionType) {
        IMessagingAggregate aggregate = event.computeType().getAggregate();
        TopicExchange exchange = exchangeProvider.getExchange(aggregate);
        String routingKey = aggregate.getKey() + "." + eventActionType.name().toLowerCase();

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
}
