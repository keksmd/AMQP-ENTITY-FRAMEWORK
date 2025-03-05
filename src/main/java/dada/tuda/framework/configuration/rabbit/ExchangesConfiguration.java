package dada.tuda.framework.configuration.rabbit;

import dada.tuda.framework.ExchangeProvider;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;
@Configuration
public class ExchangesConfiguration {
    @Bean
    static BeanDefinitionRegistryPostProcessor ex(@Autowired List<IMessagingAggregate> aggregates) {
        return new PP(aggregates);
    }

    static void registerExchange(String exchangeName, String exchangeBeanName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopicExchange.class,
                () -> ExchangeBuilder.topicExchange(exchangeName).durable(true).build());

        BeanDefinition exchangeBeanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition(exchangeBeanName, exchangeBeanDefinition);
    }

    static void registerExchangeName(String exchangeName, String exchangeBeanName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder2 = BeanDefinitionBuilder.genericBeanDefinition(String.class,
                () -> exchangeName);

        BeanDefinition nameBeanDefinition = builder2.getBeanDefinition();
        registry.registerBeanDefinition(exchangeBeanName + "Name", nameBeanDefinition);
    }


    @Bean
    ExchangeProvider exchangeProvider(org.springframework.context.ApplicationContext context, List<TopicExchange> topics) {
        return new ExchangeProvider(topics, context);
    }
}


@RequiredArgsConstructor
class PP implements Ordered, BeanDefinitionRegistryPostProcessor {
    private final List<IMessagingAggregate> aggregates;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        for (IMessagingAggregate aggregate : aggregates) {
            String exchangeBeanName = aggregate.getName().toLowerCase() + "Exchange";
            String exchangeName = aggregate.getExchangeName();
            ExchangesConfiguration.registerExchange(exchangeName, exchangeBeanName, registry);
            ExchangesConfiguration.registerExchangeName(exchangeName, exchangeBeanName, registry);
        }
    }

}
