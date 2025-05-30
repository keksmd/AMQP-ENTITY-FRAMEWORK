package dada.tuda.framework.configuration.auto.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.configuration.CancelConfig;
import dada.tuda.framework.configuration.auto.MessagingRepositoriesMissingAnnotationChecker;
import dada.tuda.framework.consistency.MessageRepository;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.RedisCachingIdempotencyProvider;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "dada.tuda.messaging.saga.enabled", havingValue = "true")
@Import(CancelConfig.class)
@ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
public class SagaConfig {
    @Bean
    @Primary
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    MessageStorage eventStorager(MessageRepository repo, MessageMapper mapper) {
        return new RedisCachingIdempotencyProvider(repo, mapper);
    }


    @Bean
    @ConditionalOnClass(AutoConfigurationPackages.class)
    MessagingRepositoriesMissingAnnotationChecker checker() {
        return new MessagingRepositoriesMissingAnnotationChecker();
    }


    @Bean
    MapToJsonConverter mapToJsonConverter(ObjectMapper objectMapper) {
        return new MapToJsonConverter(objectMapper);
    }


}
