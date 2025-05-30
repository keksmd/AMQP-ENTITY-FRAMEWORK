package dada.tuda.framework.configuration;

import dada.tuda.framework.annotations.CacheWithDetailsAspect;
import dada.tuda.framework.annotations.DetailedCacheEvictRedisAspect;
import dada.tuda.framework.cache.CacheNamesRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfiguration(after = RedisAutoConfiguration.class)
public class MyCacheAspectConfiguration {
    @Bean
    @ConditionalOnBean({ RedisConnectionFactory.class })
    public DetailedCacheEvictRedisAspect detailedCacheEvictAspect(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        return new DetailedCacheEvictRedisAspect(cacheManager, redisTemplate);
    }

    @Bean
    CacheNamesRegistry cacheNamesRegistry() {
        return new CacheNamesRegistry();
    }

    @Bean
    ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
    @Bean
    @ConditionalOnBean({ RedisConnectionFactory.class })
    public CacheWithDetailsAspect cacheWithDetailsAspect(ExecutorService executorService, @Autowired CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate, RedisCacheConfiguration redisCacheConfiguration) {
        return new CacheWithDetailsAspect(cacheManager, redisTemplate, redisCacheConfiguration, executorService);
    }

}
