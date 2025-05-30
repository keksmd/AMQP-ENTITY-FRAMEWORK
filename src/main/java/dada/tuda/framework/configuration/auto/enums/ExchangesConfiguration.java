package dada.tuda.framework.configuration.auto.enums;

import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.handling.ExchangesByDomainCreatePostProcessor;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ExchangesConfiguration {
    @Bean
    static BeanDefinitionRegistryPostProcessor ex(@Autowired List<IMessagingDomain> aggregates, ExchangeContext exchangeContext) {
        return new ExchangesByDomainCreatePostProcessor(aggregates, exchangeContext);
    }

    @Bean
    ExchangeContext exchangeProvider(List<TopicExchange> topics) {
        return new ExchangeContext(topics);
    }
}


