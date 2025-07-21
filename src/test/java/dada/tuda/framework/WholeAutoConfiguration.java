package dada.tuda.framework;

import dada.tuda.framework.configuration.BeanUtilsConfig;
import dada.tuda.framework.configuration.CancelConfig;
import dada.tuda.framework.configuration.EnumBeanConfiguration;
import dada.tuda.framework.configuration.MessagingConfiguration;
import dada.tuda.framework.configuration.PropsConfig;
import dada.tuda.framework.configuration.RedisRepositoryConfig;
import dada.tuda.framework.configuration.SagaConfig;
import dada.tuda.framework.configuration.TracingAspectConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

@AutoConfiguration
@ImportAutoConfiguration(value = {
        TracingAspectConfiguration.class,
        MessagingConfiguration.class,
        SagaConfig.class,
        RedisRepositoryConfig.class,
        BeanUtilsConfig.class,
        CancelConfig.class,
        EnumBeanConfiguration.class,
        PropsConfig.class })
public class WholeAutoConfiguration {
}
