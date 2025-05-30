package dada.tuda.framework.annotations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;

import static dada.tuda.framework.utils.FrameworkUtils.resolveKey;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class DetailedCacheEvictRedisAspect {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, ?> redisTemplate;


    @Around("@annotation(detailedCacheEvict)")
    public Object handleCacheWithDetails(ProceedingJoinPoint joinPoint, DetailedCacheEvict detailedCacheEvict) throws Throwable {
        Object key = resolveKey(joinPoint, detailedCacheEvict.listKey());
        Cache listCache = cacheManager.getCache(detailedCacheEvict.listCacheName());
        Cache itemCache = cacheManager.getCache(detailedCacheEvict.itemCacheName());

        Object result = joinPoint.proceed();

        if (itemCache != null) {
            itemCache.evictIfPresent(key);
        }
        if (listCache instanceof RedisCache redisListCache) {
            String pattern = detailedCacheEvict.listCacheName() + "::*";
            for (var k : redisTemplate.keys(pattern)) {
                if (k.split("::")[1].contains(key.toString())) {
                    redisListCache.evict(k.split("::")[1]);
                }
            }
        }

        return result;
    }
}

