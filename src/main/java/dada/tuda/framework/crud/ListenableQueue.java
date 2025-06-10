package dada.tuda.framework.crud;

import org.springframework.amqp.rabbit.annotation.Queue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenableQueue {
    Queue value();

    String listen() default "true";
}
