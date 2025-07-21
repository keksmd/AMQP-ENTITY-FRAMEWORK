package dada.tuda.framework.conf;

import dada.tuda.framework.normalization.types.realizations.EnableMessagingRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "dada.tuda.framework.conf.beans")
@EnableMessagingRepositories(basePackages = "dada.tuda.framework.conf.beans")
public class RepoConfig {
}
