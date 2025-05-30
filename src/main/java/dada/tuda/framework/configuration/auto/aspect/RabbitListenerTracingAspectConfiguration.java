package dada.tuda.framework.configuration.auto.aspect;

import dada.tuda.framework.annotations.RabbitHandlerAspect;
import dada.tuda.framework.normalization.TracingHeader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class RabbitListenerTracingAspectConfiguration {

    @Bean
    TracingHeader tracingHeader() {
        return new TracingHeader();
    }

    @Bean
    public RabbitHandlerAspect rabbitHandlerAspect() {
        return new RabbitHandlerAspect();
    }
}
