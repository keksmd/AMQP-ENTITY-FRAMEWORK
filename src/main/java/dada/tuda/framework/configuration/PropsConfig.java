package dada.tuda.framework.configuration;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PropsConfig {
    @Bean
    @ConfigurationProperties(prefix = "dada.tuda.framework")
    public DadaTudaFrameworkProperties frameworkProperties() {
        return new DadaTudaFrameworkProperties();
    }
}
