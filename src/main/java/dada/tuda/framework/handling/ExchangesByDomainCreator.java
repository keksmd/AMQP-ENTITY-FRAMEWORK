package dada.tuda.framework.handling;

import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.SmartInitializingSingleton;

@Slf4j
public class ExchangesByDomainCreator implements SmartInitializingSingleton {
    private final DomainContext domainContext;
    private final ExchangeContext exchangeContext;
    private final RabbitAdmin rabbitAdmin;

    public ExchangesByDomainCreator(ConnectionFactory connectionFactory, DomainContext domainContext, ExchangeContext exchangeContext) {
        this.domainContext = domainContext;
        this.exchangeContext = exchangeContext;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);

    }

    @Override
    public void afterSingletonsInstantiated() {

    }
}
