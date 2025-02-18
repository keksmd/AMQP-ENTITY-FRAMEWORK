package dada.tuda.framework.configuration.rabbit;

import dada.tuda.framework.ExchangeProvider;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class ExchangesConfiguration {
    @Bean
    static BeanDefinitionRegistryPostProcessor ex(@Autowired List<IMessagingAggregate> aggregates) {


        return registry -> {
            for (IMessagingAggregate aggregate : aggregates) {
                String exchangeBeanName = aggregate.getName().toLowerCase() + "Exchange";
                String exchangeName = aggregate.getExchangeName();
                registerExchange(exchangeName, exchangeBeanName, registry);
                registerExchangeName(exchangeName, exchangeBeanName, registry);
            }
        };
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
