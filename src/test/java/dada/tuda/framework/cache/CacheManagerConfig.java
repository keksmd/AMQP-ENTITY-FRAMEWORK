package dada.tuda.framework.cache;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@TestConfiguration
public class CacheManagerConfig {
    @Bean
    RedisCacheManager redisCacheManagerWithJsonSerializer(CacheNamesRegistry cacheNamesRegistry, RedisConnectionFactory connectionFactory, RedisCacheConfiguration config) {
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(config)
                .initialCacheNames(cacheNamesRegistry.getCacheNames())
                .build();
    }
}
