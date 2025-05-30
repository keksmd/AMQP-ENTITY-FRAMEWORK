package dada.tuda.framework.springconf;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DadaTudaFrameworkProperties.class)
public class PropertiesConfig {
}
