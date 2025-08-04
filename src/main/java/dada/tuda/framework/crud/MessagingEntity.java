package dada.tuda.framework.crud;

import org.springframework.amqp.rabbit.annotation.Queue;
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

    String domain();


    String createDefaultBindings() default "true";


    Queue[] queues() default {};

    String ttl() default "60000";
}
