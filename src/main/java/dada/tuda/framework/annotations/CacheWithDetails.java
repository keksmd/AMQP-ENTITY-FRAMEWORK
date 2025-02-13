package dada.tuda.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheWithDetails {
    String listCache(); // Имя кэша для списка

    String itemCache(); // Имя кэша для каждого элемента

    String listKey() default "";// Поле или выражение для создания ключа списка

    String itemKeyField() default "id"; // Поле элемента, используемое как ключ
}
