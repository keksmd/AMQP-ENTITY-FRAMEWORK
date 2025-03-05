package dada.tuda.framework.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntegrationLogConfiguration {

    @Bean
    public IntegrationLogAspect integrationLogAspect() {
        return new IntegrationLogAspect();
    }
}
