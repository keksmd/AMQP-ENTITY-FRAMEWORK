package dada.tuda.framework.configuration.aspect;

import dada.tuda.framework.annotations.CacheWithDetailsAspect;
import dada.tuda.framework.configuration.redis.publiced.CustomCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@EnableAspectJAutoProxy
@Configuration
@Import(CustomCacheConfig.class)
public class CacheWithDetailsAspectConfiguration {

    @Bean
    public CacheWithDetailsAspect cacheWithDetailsAspect(@Autowired CacheManager cacheManager) {
        return new CacheWithDetailsAspect(cacheManager);
    }
}
