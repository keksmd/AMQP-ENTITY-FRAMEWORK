package dada.tuda.framework.configuration.aspect.publiced;

import dada.tuda.framework.configuration.aspect.ControllerTracingAspectConfiguration;
import dada.tuda.framework.configuration.aspect.RabbitListenerTracingAspectConfiguration;
import org.springframework.context.annotation.Import;

@Import({ControllerTracingAspectConfiguration.class, RabbitListenerTracingAspectConfiguration.class})
public class CustomTracingConfig {
}
