package dada.tuda.framework.configuration.aspect.publiced;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@Import(EnumBeanRegistrar.class)
public class EnumBeanConfiguration {
}