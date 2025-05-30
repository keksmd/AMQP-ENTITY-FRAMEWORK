package dada.tuda.framework.configuration.auto.aspect.publiced;

import dada.tuda.framework.configuration.auto.aspect.ControllerTracingAspectConfiguration;
import dada.tuda.framework.configuration.auto.aspect.RabbitListenerTracingAspectConfiguration;
import org.springframework.context.annotation.Import;

@Import({ ControllerTracingAspectConfiguration.class, RabbitListenerTracingAspectConfiguration.class })
public class CustomTracingConfig {
}
