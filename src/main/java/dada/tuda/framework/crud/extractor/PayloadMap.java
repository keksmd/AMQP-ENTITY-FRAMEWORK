package dada.tuda.framework.crud.extractor;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface PayloadMap {
    @AliasFor("value")
    String replace() default "false";

    @AliasFor("replace")
    String value() default "false";

    String rewriteValues() default "false";
}
