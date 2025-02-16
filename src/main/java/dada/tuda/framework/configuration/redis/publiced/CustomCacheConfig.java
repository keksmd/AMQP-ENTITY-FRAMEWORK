package dada.tuda.framework.configuration.redis.publiced;

import dada.tuda.framework.configuration.redis.RedisConnectionConfig;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Import(RedisConnectionConfig.class)
@Configuration
public class CustomCacheConfig {
    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory, GenericJackson2JsonRedisSerializer serializer) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Время жизни кеша
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
