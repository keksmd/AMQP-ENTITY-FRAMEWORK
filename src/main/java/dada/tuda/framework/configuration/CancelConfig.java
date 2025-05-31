package dada.tuda.framework.configuration;

import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import dada.tuda.framework.normalization.types.realizations.EnableMessagingRepositories;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class CancelConfig {
    @Bean
    public CancelPayload cancelPayload() {
        return new CancelPayload();
    }

    @Configuration
    @EnableMessagingRepositories(basePackages = "dada.tuda.framework.repositories.cancel")
    @ConditionalOnBean(ConnectionFactory.class)
    public static class CancelRepositoryConfig {
    }
}
