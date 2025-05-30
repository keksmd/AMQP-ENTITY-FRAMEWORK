package dada.tuda.framework.configuration;

import dada.tuda.framework.annotations.RequestMdcAspect;
import dada.tuda.framework.normalization.TracingHeader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@AutoConfiguration
public class TracingAspectConfiguration {

    @Bean
    public RequestMdcAspect controllerTracingAspect() {
        return new RequestMdcAspect();
    }

    @Bean
    TracingHeader tracingHeader() {
        return new TracingHeader();
    }
}
