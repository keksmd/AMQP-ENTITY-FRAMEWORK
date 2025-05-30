package dada.tuda.framework.handling;

import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ExchangesByDomainCreatePostProcessor implements Ordered, BeanDefinitionRegistryPostProcessor {
    private final List<IMessagingDomain> domains;
    private final ExchangeContext exchangeContext;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (IMessagingDomain iMessagingDomain : domains) {
            String exchangeBeanName = iMessagingDomain.getName().toLowerCase() + "Exchange";
            String exchangeName = iMessagingDomain.getExchangeName();
            if (registry.containsBeanDefinition(exchangeBeanName) || registry.containsBeanDefinition(exchangeName)) {
                log.warn("Duplicated  exchange name for exchange {} ,named {}", exchangeBeanName, exchangeName);
            } else {
                var exchange = registerExchange(exchangeName, exchangeBeanName, registry);
                registerExchangeName(exchangeName, exchangeBeanName, registry);
                exchangeContext.registerExchange(exchange);
            }

        }
    }

    static TopicExchange registerExchange(String exchangeName, String exchangeBeanName, BeanDefinitionRegistry registry) {
        TopicExchange topic = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopicExchange.class,
                () -> topic);
        BeanDefinition exchangeBeanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition(exchangeBeanName, exchangeBeanDefinition);
        return topic;
    }

    static void registerExchangeName(String exchangeName, String exchangeBeanName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder2 = BeanDefinitionBuilder.genericBeanDefinition(String.class,
                () -> exchangeName);

        BeanDefinition nameBeanDefinition = builder2.getBeanDefinition();
        registry.registerBeanDefinition(exchangeBeanName + "Name", nameBeanDefinition);
    }

}
