package dada.tuda.framework.configuration.aspect.publiced;

import dada.tuda.framework.configuration.aspect.CacheWithDetailsAspectConfiguration;
import dada.tuda.framework.configuration.aspect.DetailedCacheEvictAspectConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CacheWithDetailsAspectConfiguration.class, DetailedCacheEvictAspectConfiguration.class})
public class CustomCacheAnnotationsConfig {
}
