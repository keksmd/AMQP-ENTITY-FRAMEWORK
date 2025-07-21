package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;

@AutoConfiguration(after = RedisAutoConfiguration.class)
public class RedisRepositoryConfig {
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
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
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
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

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    @Primary
    RedisCacheConfiguration redisCacheConfigurationWithJsonSerializer(GenericJackson2JsonRedisSerializer serializer, @Value("${spring.cache.redis.time-to-live:#{10*60*1000}}") Integer ttl) {
        RedisSerializationContext.SerializationPair<String> keySer =
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
        RedisSerializationContext.SerializationPair<Object> valueSer = RedisSerializationContext.SerializationPair.fromSerializer(serializer);
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttl))
                .serializeKeysWith(keySer)
                .serializeValuesWith(valueSer);
    }

    @Configuration
    @EnableRedisRepositories(
            basePackages = "dada.tuda.framework.consistency",
            enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP
    )
    @ConditionalOnBean(RedisConnectionFactory.class)
    public static class RedisRepositoriesConfig {

    }
}
