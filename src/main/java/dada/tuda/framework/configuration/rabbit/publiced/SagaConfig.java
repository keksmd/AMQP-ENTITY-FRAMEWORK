package dada.tuda.framework.configuration.rabbit.publiced;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.configuration.jackson.JaksonConfiguration;
import dada.tuda.framework.configuration.redis.RedisRepositoryConfig;
import dada.tuda.framework.consistency.EventRepository;
import dada.tuda.framework.consistency.EventStorager;
import dada.tuda.framework.consistency.RedisEventStorager;
import dada.tuda.framework.normalization.converters.EventTypeConverter;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@Import({RedisRepositoryConfig.class, JaksonConfiguration.class, MessagingConfiguration.class})
public class SagaConfig {
    @Bean
    @Primary
    EventStorager eventStorager(EventRepository repo) {
        return new RedisEventStorager(repo);
    }

    @Bean
    MapToJsonConverter mapToJsonConverter(ObjectMapper objectMapper) {
        return new MapToJsonConverter(objectMapper);
    }

    @Bean
    EventTypeConverter eventTypeConverter(@Autowired List<IMessagingEventType> messagingEventTypes) {
        return new EventTypeConverter(messagingEventTypes);
    }


}
