package dada.tuda.framework.configuration;

import dada.tuda.framework.normalization.types.realizations.EnableMessagingRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMessagingRepositories(basePackages = "dada.tuda.framework.repositories.cancel")
@ComponentScan(basePackages = "dada.tuda.framework.repositories.cancel")
public class CancelConfig {
}
