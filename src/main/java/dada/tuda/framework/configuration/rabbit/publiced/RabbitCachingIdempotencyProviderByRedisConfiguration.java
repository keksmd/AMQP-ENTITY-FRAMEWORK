package dada.tuda.framework.configuration.rabbit.publiced;

import dada.tuda.framework.configuration.redis.RedisConnectionConfig;
import dada.tuda.framework.consistency.IdempotencyProvider;
import dada.tuda.framework.consistency.RedisCachingIdempotencyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(RedisConnectionConfig.class)
@Configuration
public class RabbitCachingIdempotencyProviderByRedisConfiguration {
    @Bean
    @Primary
    IdempotencyProvider redisCachingIdempotencyProvider(StringRedisTemplate stringRedisTemplate) {
        return new RedisCachingIdempotencyProvider(stringRedisTemplate);
    }


}
