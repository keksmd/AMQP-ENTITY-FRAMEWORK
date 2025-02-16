package dada.tuda.framework.annotations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static dada.tuda.framework.FrameworkUtils.extractKey;
import static dada.tuda.framework.FrameworkUtils.isReturnsList;
import static dada.tuda.framework.FrameworkUtils.resolveKey;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class CacheWithDetailsAspect {

    private final CacheManager cacheManager;


    @Around("@annotation(cacheWithDetails)")
    public Object handleCacheWithDetails(ProceedingJoinPoint joinPoint, CacheWithDetails cacheWithDetails) throws Throwable {
        boolean returnsList = isReturnsList(joinPoint);
        Cache listCache = cacheManager.getCache(cacheWithDetails.listCache());
        Collection<Object> keys = (Collection<Object>) resolveKey(joinPoint, cacheWithDetails.listKey());
        Cache itemCache = cacheManager.getCache(cacheWithDetails.itemCache());


        try {
            return Objects.requireNonNull(listCache.get(keys)).get();
        } catch (NullPointerException e) {
        }


        Collection<Object> ans = returnsList ? new ArrayList<>() : new HashSet<>();
        if (itemCache != null) {
            for (Object itemK : keys) {
                if (itemK != null) {
                    var item = itemCache.get(itemK);
                    if (item != null) {
                        ans.add(item.get());
                    }
                }
            }
            if (ans.size() == keys.size()) {
                return ans;
            } else {
                ans.clear();
            }
        }

        Object result = joinPoint.proceed();

        if (listCache != null && result instanceof List<?> list) {
            listCache.put(keys, list);
        }
        if (itemCache != null && result instanceof List<?> list) {
            try {
                list.forEach(item -> {
                    String itemKey = extractKey(item, cacheWithDetails.itemKeyField());
                    if (itemKey != null) {
                        itemCache.put(itemKey, item);
                    }
                });
            } catch (IllegalStateException e) {
                log.error("Cannot extract key by field-name {} from class {} ", cacheWithDetails.itemKeyField(), list.getClass().getComponentType());
            }
        }
        return result;
    }


}
