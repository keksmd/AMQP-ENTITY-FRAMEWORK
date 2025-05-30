package dada.tuda.framework.conf;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.RabbitMQContainer;


@Slf4j
@TestConfiguration
@EnableRabbit
public class RabbitContainerConfig {
    private static final RabbitMQContainer rabbitMQContainer;

    static {
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-alpine");
        rabbitMQContainer.start();
        System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port",
                String.valueOf(rabbitMQContainer.getMappedPort(5672)));
        System.setProperty("spring.rabbitmq.username", rabbitMQContainer.getAdminUsername());
        System.setProperty("spring.rabbitmq.password", rabbitMQContainer.getAdminPassword());
        System.setProperty("spring.rabbitmq.virtual-host", "/");
    }


    @PreDestroy
    public void stopContainer() {
        if (rabbitMQContainer.isRunning()) {
            rabbitMQContainer.stop();
        }
    }

    @Bean
    public ConnectionFactory connectionFactory(Environment environment) {
        String host = environment.getProperty("spring.rabbitmq.host");
        int port = Integer.parseInt(environment.getProperty("spring.rabbitmq.port"));
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(rabbitMQContainer.getAdminUsername());
        factory.setPassword(rabbitMQContainer.getAdminPassword());
        return factory;
    }

}
