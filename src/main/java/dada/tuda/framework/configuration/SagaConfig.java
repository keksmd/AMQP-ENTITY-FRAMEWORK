package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.MessagingRepositoriesMissingAnnotationChecker;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.facade.MessageCanceller;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class SagaConfig {

    @Bean
    @ConditionalOnClass(AutoConfigurationPackages.class)
    @ConditionalOnBean({ ConnectionFactory.class })
    MessagingRepositoriesMissingAnnotationChecker checker() {
        return new MessagingRepositoriesMissingAnnotationChecker();
    }


    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @DependsOn("objectMapperForRabbitEntities")
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    public MessageCanceller eventCanceler(MessageSender sender, @Qualifier("objectMapperForRabbitEntities") ObjectMapper objectMapper, IEventActionContext entityContext) {
        return new MessageCanceller(entityContext, sender, objectMapper);
    }

    @Bean
    @ConditionalOnBean({ RedisConnectionFactory.class, ConnectionFactory.class })
    @DependsOn("objectMapperForRedis")
    @ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    MapToJsonConverter mapToJsonConverter(@Qualifier("objectMapperForRedis") ObjectMapper objectMapper) {
        return new MapToJsonConverter(objectMapper);
    }


}
