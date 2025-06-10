package dada.tuda.framework.crud;

import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MessagingEntity {
    @AliasFor("value")
    String domain() default "";

    @AliasFor("domain")
    String value() default "";

    String createDefaultBindings() default "true";


    Queue[] queues() default {};

    String getTtl() default "60000";
}
