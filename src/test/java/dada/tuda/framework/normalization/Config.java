package dada.tuda.framework.normalization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@TestConfiguration
public class Config{
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(Jackson2JsonMessageConverter converter, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(converter);
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3); // Настройте количество потоков
        factory.setMaxConcurrentConsumers(10); // Максимальное количество потоков
        factory.setPrefetchCount(10); // Настройте prefetch
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory(
                                               @Value("${spring.rabbitmq.host}") String host,
                                               @Value("${spring.rabbitmq.port}") Integer port) {
        CachingConnectionFactory factory = new CachingConnectionFactory(
                host, port
        );
        factory.setUsername(IntegrationTest.rabbitMQContainer.getAdminUsername());
        factory.setPassword(IntegrationTest.rabbitMQContainer.getAdminPassword());

        return factory;
    }
}





