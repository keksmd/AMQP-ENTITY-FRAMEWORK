package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.MessagingRepositoriesMissingAnnotationChecker;
import dada.tuda.framework.consistency.MessageRepository;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.RedisCachingIdempotencyProvider;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class SagaConfig {
    @Bean
    @Primary
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    MessageStorage eventStorager(MessageRepository repo, MessageMapper mapper) {
        return new RedisCachingIdempotencyProvider(repo, mapper);
    }


    @Bean
    @ConditionalOnClass(AutoConfigurationPackages.class)
    @ConditionalOnBean({ ConnectionFactory.class })
    MessagingRepositoriesMissingAnnotationChecker checker() {
        return new MessagingRepositoriesMissingAnnotationChecker();
    }


    @Bean
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    MapToJsonConverter mapToJsonConverter(ObjectMapper objectMapper) {
        return new MapToJsonConverter(objectMapper);
    }


}
