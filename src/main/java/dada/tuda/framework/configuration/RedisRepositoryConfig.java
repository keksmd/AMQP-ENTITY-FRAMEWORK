package dada.tuda.framework.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dada.tuda.framework.consistency.MessageRepository;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.consistency.RedisCachingIdempotencyProvider;
import dada.tuda.framework.consistency.mapper.RedisMapper;
import dada.tuda.framework.consistency.mapper.RedisMapperImpl;
import dada.tuda.framework.crud.contexts.DomainContext;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

@ConditionalOnClass(RedisOperations.class)
@AutoConfiguration(after = { RedisAutoConfiguration.class, JacksonAutoConfiguration.class })
public class RedisRepositoryConfig {
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnClass(RedisOperations.class)
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(@Qualifier("objectMapperForRedis") ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper) {
            private static final byte[] BINARY_NULL;

            static {
                BINARY_NULL = RedisSerializer.java().serialize(NullValue.INSTANCE);
            }

            private final GenericJackson2JsonRedisSerializer delegate = new GenericJackson2JsonRedisSerializer(objectMapper);

            @Override
            public byte[] serialize(Object o) throws SerializationException {
                return delegate.serialize(o);
            }

            @Override
            public Object deserialize(@Nullable byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                // Перехватываем байты JDK-сериализованного NullValue
                if (Arrays.equals(bytes, BINARY_NULL)) {
                    return null;
                }
                try {
                    return delegate.deserialize(bytes);
                } catch (SerializationException e) {
                    // Если это явно не JSON — подавим
                    if (isLikelyNotJson(bytes)) {
                        return null;
                    }
                    throw e;
                }
            }

            private boolean isLikelyNotJson(byte[] bytes) {
                byte first = bytes[0];
                return !(first == '{' || first == '[' || first == '"');
            }
        };
    }

    @Bean
    @ConditionalOnClass(RedisOperations.class)
    public RedisMapper redisMapper(DomainContext domainContext) {
        var mapper = new RedisMapperImpl();
        mapper.domainContext = domainContext;
        return mapper;
    }

    @Bean
    @Primary
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnClass(RedisOperations.class)
    //@ConditionalOnProperty(name = "dada.tuda.framework.messaging.saga.enabled", havingValue = "true")
    public MessageStorage eventStorager(MessageRepository repo, RedisMapper mapper) {
        return new RedisCachingIdempotencyProvider(repo, mapper);
    }

    @Bean
    @ConditionalOnClass(RedisOperations.class)
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




    @Bean
    @ConditionalOnClass(RedisOperations.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnClass(RedisOperations.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    RedisTemplate<String, Object> redisTemplateWithJsonSerializer(RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer serializer) {
        var redis = new RedisTemplate<String, Object>();
        redis.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redis.setKeySerializer(stringSerializer);
        redis.setHashKeySerializer(stringSerializer);
        redis.setValueSerializer(serializer);
        redis.setHashValueSerializer(serializer);
        redis.setDefaultSerializer(serializer);
        redis.setEnableDefaultSerializer(true);
        redis.afterPropertiesSet();
        return redis;
    }


    @Configuration
    @ConditionalOnClass(RedisOperations.class)
    @EnableRedisRepositories(
            basePackages = "dada.tuda.framework.consistency",
            enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP
    )
    @ConditionalOnBean(RedisConnectionFactory.class)
    public static class RedisRepositoriesConfig {

    }
}
