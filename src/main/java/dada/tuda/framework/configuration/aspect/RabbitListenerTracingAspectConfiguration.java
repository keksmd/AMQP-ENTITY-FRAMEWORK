package dada.tuda.framework.configuration.aspect;

import dada.tuda.framework.annotations.RabbitHandlerAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class RabbitListenerTracingAspectConfiguration {


    @Bean
    public RabbitHandlerAspect rabbitHandlerAspect() {
        return new RabbitHandlerAspect();
    }
}
