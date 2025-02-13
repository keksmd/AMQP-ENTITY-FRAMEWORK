package dada.tuda.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DetailedCacheEvict {
    String listCacheName();

    String itemCacheName();

    String listKey() default "";
}
