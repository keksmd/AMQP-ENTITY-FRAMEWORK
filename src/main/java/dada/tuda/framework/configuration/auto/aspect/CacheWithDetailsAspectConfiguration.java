package dada.tuda.framework.configuration.auto.aspect;

import dada.tuda.framework.annotations.CacheWithDetailsAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ExecutorService;

@EnableAspectJAutoProxy
@Configuration
public class CacheWithDetailsAspectConfiguration {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheWithDetailsAspect cacheWithDetailsAspect(ExecutorService executorService, @Autowired CacheManager cacheManager, RedisTemplate redisTemplate, RedisCacheConfiguration redisCacheConfiguration) {
        return new CacheWithDetailsAspect(cacheManager, redisTemplate, redisCacheConfiguration, executorService);
    }
}
