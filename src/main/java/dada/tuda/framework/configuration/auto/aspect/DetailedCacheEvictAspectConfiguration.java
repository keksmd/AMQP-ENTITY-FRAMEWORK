package dada.tuda.framework.configuration.auto.aspect;

import dada.tuda.framework.annotations.DetailedCacheEvictRedisAspect;
import dada.tuda.framework.configuration.auto.redis.CustomCacheConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Import({ CustomCacheConfig.class })
@Configuration
public class DetailedCacheEvictAspectConfiguration {
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public DetailedCacheEvictRedisAspect detailedCacheEvictAspect(CacheManager cacheManager, RedisTemplate<String, ?> redisTemplate) {
        return new DetailedCacheEvictRedisAspect(cacheManager, redisTemplate);
    }

}
