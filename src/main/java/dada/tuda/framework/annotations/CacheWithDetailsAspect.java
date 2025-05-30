package dada.tuda.framework.annotations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static dada.tuda.framework.utils.FrameworkUtils.extractKey;
import static dada.tuda.framework.utils.FrameworkUtils.resolveKey;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class CacheWithDetailsAspect {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheConfiguration redisCacheConfiguration;
    private final ExecutorService executor;


    @Around("@annotation(cacheWithDetails)")
    public Object handleCacheWithDetails(ProceedingJoinPoint joinPoint, CacheWithDetails cacheWithDetails) throws Throwable {
        Cache listCache = cacheManager.getCache(cacheWithDetails.listCache());
        Collection<Object> keys = (Collection<Object>) resolveKey(joinPoint, cacheWithDetails.listKey());
        Cache itemCache = cacheManager.getCache(cacheWithDetails.itemCache());

        try {
            return Objects.requireNonNull(listCache.get(keys)).get();
        } catch (NullPointerException ignored) {
        }
        Collection<Object> ans;
        if (itemCache != null) {
            List<String> keysStr = keys.stream().map(this::serializeKey).map(k -> itemCache.getName() + "::" + k).toList();
            ans = redisTemplate.opsForValue().multiGet(keysStr);
            if (ans != null && ans.stream().filter(Objects::nonNull).count() == keys.size()) {
                return ans;
            }
        }

        Object result = joinPoint.proceed();
        var future = executor.submit(() -> {
            if (listCache != null && result instanceof List<?> list) {
                listCache.put(keys, list);
            }
            if (itemCache != null && result instanceof List<?> list) {
                try {
                    Map<String, Object> items = new HashMap<>();
                    list.forEach(item -> {
                        String origKey = extractKey(item, cacheWithDetails.itemKeyField());
                        String itemKey = itemCache.getName() + "::" + origKey;
                        items.put(itemKey, item);
                    });
                    redisTemplate.opsForValue().multiSet(items);
                } catch (IllegalStateException e) {
                    log.error("Cannot extract key by field-name {} from class {} ", cacheWithDetails.itemKeyField(), list.getClass().getComponentType());
                }
            }
        });
        if ("true".equals(cacheWithDetails.waitSet())) {
            future.get();
        }

        return result;
    }

    private String serializeKey(Object key) {
        return new String(ByteUtils.getBytes(redisCacheConfiguration.getKeySerializationPair().write(this.convertSingleKey(key))));
    }

    private String convertSingleKey(Object key) {
        if (key instanceof String k) {
            return k;
        }
        TypeDescriptor source = TypeDescriptor.forObject(key);

        if (redisCacheConfiguration.getConversionService().canConvert(source, TypeDescriptor.valueOf(String.class))) {
            return redisCacheConfiguration.getConversionService().convert(key, String.class);
        }
        Method toString = ReflectionUtils.findMethod(key.getClass(), "toString");
        if (toString != null && !Object.class.equals(toString.getDeclaringClass())) {
            return key.toString();
        }
        throw new IllegalStateException(String.format("Cannot convert cache key %s to String; Please register a suitable Converter via 'RedisCacheConfiguration.configureKeyConverters(...)' or override '%s.toString()'", source, key.getClass().getSimpleName()));
    }

    private byte[] serializeValue(Object item) {
        return ByteUtils.getBytes(this.redisCacheConfiguration.getValueSerializationPair().write(item));
    }


}
