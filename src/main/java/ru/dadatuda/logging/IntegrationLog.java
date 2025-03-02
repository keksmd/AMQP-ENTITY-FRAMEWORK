package dada.tuda.framework.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationLog {

    String eventType();

    Direction direction();

    enum Direction {
        IN,
        OUT
    }
}
