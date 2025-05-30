package dada.tuda.framework.configuration.auto.redis;

import dada.tuda.framework.cache.CacheNamesRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CustomCacheConfig {
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    RedisCacheManager redisCacheManager(CacheNamesRegistry cacheNamesRegistry, RedisConnectionFactory connectionFactory, RedisCacheConfiguration config) {
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(config)
                .initialCacheNames(cacheNamesRegistry.getCacheNames())
                .build();
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    RedisCacheConfiguration redisCacheConfiguration(GenericJackson2JsonRedisSerializer serializer, @Value("${spring.cache.redis.time-to-live:#{10*60*1000}}") Integer ttl) {
        RedisSerializationContext.SerializationPair<String> keySer =
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
        RedisSerializationContext.SerializationPair<Object> valueSer = RedisSerializationContext.SerializationPair.fromSerializer(serializer);
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttl))
                .serializeKeysWith(keySer)
                .serializeValuesWith(valueSer);
    }
}