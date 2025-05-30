package dada.tuda.framework.conf;

import dada.tuda.framework.normalization.types.realizations.EnableMessagingRepositories;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "dada.tuda.framework.conf.beans")
@EnableMessagingRepositories(basePackages = "dada.tuda.framework.conf.beans")
public class RepoConfig {
}
