package dada.tuda.framework.springconf;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(DadaTudaFrameworkProperties.class)
@ConfigurationPropertiesScan("dada.tuda.framework")
public class PropertiesConfig {
}
