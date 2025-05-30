package dada.tuda.framework.cache;

import dada.tuda.framework.annotations.CacheWithDetails;
import dada.tuda.framework.annotations.DetailedCacheEvict;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class CacheNamesRegistry implements ApplicationContextAware {

    private final Set<String> cacheNames = new HashSet<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getMethods()) {
                CacheWithDetails annotation = AnnotationUtils.findAnnotation(method, CacheWithDetails.class);
                if (annotation != null) {
                    cacheNames.add(annotation.listCache());
                    cacheNames.add(annotation.itemCache());
                }
                DetailedCacheEvict detailedCacheEvict = AnnotationUtils.findAnnotation(method, DetailedCacheEvict.class);
                if (detailedCacheEvict != null) {
                    cacheNames.add(detailedCacheEvict.listCacheName());
                    cacheNames.add(detailedCacheEvict.listCacheName());
                }
            }
        }
    }

}