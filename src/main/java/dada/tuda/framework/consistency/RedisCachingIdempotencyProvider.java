package dada.tuda.framework.consistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class RedisCachingIdempotencyProvider implements IdempotencyProvider {
    private final StringRedisTemplate redisTemplate;
    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public boolean eventProcessed(String operationId) {
        try {
            return redisTemplate.hasKey(buildKey(operationId));
        } catch (Exception e) {
            log.error("failed to check event processed {}", e.getMessage());
            return false;
        }
    }

    protected String buildKey(String operationId) {
        return serviceName + ":" + operationId;
    }

    @Override
    public void storeEventAsProcessed(String id) {
        try {
            String key = buildKey(id);
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set(key, key, 5, TimeUnit.MINUTES);
            log.debug("saved event processed {}", key);
        } catch (Exception e) {
            log.error("failed to save event processed {}", e.getMessage());
        }
    }


}
