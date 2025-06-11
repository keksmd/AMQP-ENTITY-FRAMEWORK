package dada.tuda.framework.handling;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ActionHandler {
    @AliasFor("value")
    String[] action() default {};

    @AliasFor("action")
    String[] value() default {};

    String cancelMethod() default "";
}
