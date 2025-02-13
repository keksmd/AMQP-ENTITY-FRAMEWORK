package dada.tuda.framework.configuration.aspect;

import dada.tuda.framework.annotations.RequestMdcAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class ControllerTracingAspectConfiguration {
    @Bean
    public RequestMdcAspect controllerTracingAspect() {
        return new RequestMdcAspect();
    }
}
