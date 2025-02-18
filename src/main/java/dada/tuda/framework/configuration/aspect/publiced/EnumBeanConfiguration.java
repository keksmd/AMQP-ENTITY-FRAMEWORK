package dada.tuda.framework.configuration.aspect.publiced;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EnumBeanRegistrar.class)
public class EnumBeanConfiguration {
}