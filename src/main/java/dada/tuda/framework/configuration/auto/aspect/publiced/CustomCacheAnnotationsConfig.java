package dada.tuda.framework.configuration.auto.aspect.publiced;

import dada.tuda.framework.cache.CacheNamesRegistry;
import dada.tuda.framework.configuration.auto.aspect.CacheWithDetailsAspectConfiguration;
import dada.tuda.framework.configuration.auto.aspect.DetailedCacheEvictAspectConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ CacheWithDetailsAspectConfiguration.class, DetailedCacheEvictAspectConfiguration.class })
public class CustomCacheAnnotationsConfig {
    @Bean
    CacheNamesRegistry cacheNamesRegistry() {
        return new CacheNamesRegistry();
    }
}
