package dada.tuda.framework.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

@AutoConfiguration(before = { JacksonAutoConfiguration.class })
public class JaksonConfiguration {

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public ObjectMapper objectMapperForRabbitEntities() {
        ObjectMapper mapper = new ObjectMapper();
        var module = new JavaTimeModule();
        mapper = mapper.registerModule(module);
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper = mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper = mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        return mapper;
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public ObjectMapper objectMapperForRedis() {
        ObjectMapper mapper = new ObjectMapper();
        var module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.enable(INCLUDE_SOURCE_IN_LOCATION);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        return mapper;
    }



}
