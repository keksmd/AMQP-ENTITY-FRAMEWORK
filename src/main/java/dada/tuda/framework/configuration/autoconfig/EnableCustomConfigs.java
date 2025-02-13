package dada.tuda.framework.configuration.autoconfig;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MyConfigurationSelector.class)
public @interface EnableCustomConfigs {
    @AliasFor("value")
    ConfigType[] types() default {};

    @AliasFor("types")
    ConfigType[] value() default {};
}
