package dada.tuda.framework.configuration.aspect;

import dada.tuda.framework.annotations.DetailedCacheEvictAspect;
import dada.tuda.framework.configuration.redis.RedisConnectionConfig;
import dada.tuda.framework.configuration.redis.publiced.CustomCacheConfig;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Import({CustomCacheConfig.class, RedisConnectionConfig.class})
@Configuration
public class DetailedCacheEvictAspectConfiguration {
    @Bean
    public DetailedCacheEvictAspect detailedCacheEvictAspect(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        return new DetailedCacheEvictAspect(cacheManager, redisTemplate);
    }
}
