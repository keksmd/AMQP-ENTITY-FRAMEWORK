package dada.tuda.framework.configuration.auto.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

@ConditionalOnBean(RedisConnectionFactory.class)
@Configuration
public class RedisConnectionConfig {
    @Bean
    @ConditionalOnBean(name = "objectMapperForRedis")
    public GenericJackson2JsonRedisSerializer serializer(@Qualifier("objectMapperForRedis") ObjectMapper objectMapper) {
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
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer serializer) {
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
}
