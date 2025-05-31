package dada.tuda.framework.handling;

import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;

import java.util.List;

@Slf4j

public class ExchangesByDomainCreatePostProcessor implements Ordered, SmartInitializingSingleton {
    private final List<IMessagingDomain> domains;
    private final ExchangeContext exchangeContext;
    private final RabbitAdmin rabbitAdmin;

    public ExchangesByDomainCreatePostProcessor(ConnectionFactory connectionFactory, List<IMessagingDomain> domains, ExchangeContext exchangeContext) {
        this.domains = domains;
        this.exchangeContext = exchangeContext;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (IMessagingDomain iMessagingDomain : domains) {
            String exchangeName = iMessagingDomain.getExchangeName();
            TopicExchange exchange = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
            exchangeContext.registerExchange(exchange);
            rabbitAdmin.declareExchange(exchange);
        }
    }
}
