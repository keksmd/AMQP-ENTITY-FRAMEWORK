package dada.tuda.framework.springconf;

import dada.tuda.framework.configuration.auto.DadaTudaFrameworkProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "dada.tuda.framework.configuration.auto")
@EnableConfigurationProperties(DadaTudaFrameworkProperties.class)
public class WholeConfig {
}
