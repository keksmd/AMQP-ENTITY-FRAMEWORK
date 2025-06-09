package dada.tuda.framework;

import dada.tuda.framework.conf.PropsConfig;
import dada.tuda.framework.configuration.BeanUtilsConfig;
import dada.tuda.framework.configuration.CancelConfig;
import dada.tuda.framework.configuration.EnumBeanConfiguration;
import dada.tuda.framework.configuration.JaksonConfiguration;
import dada.tuda.framework.configuration.MessagingConfiguration;
import dada.tuda.framework.configuration.MyCacheAspectConfiguration;
import dada.tuda.framework.configuration.RedisRepositoryConfig;
import dada.tuda.framework.configuration.SagaConfig;
import dada.tuda.framework.configuration.TracingAspectConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(value = {
        TracingAspectConfiguration.class,
        JaksonConfiguration.class,
        MyCacheAspectConfiguration.class,
        MessagingConfiguration.class,
        SagaConfig.class,
        RedisRepositoryConfig.class,
        BeanUtilsConfig.class,
        CancelConfig.class,
        EnumBeanConfiguration.class,
        PropsConfig.class })
public class WholeConfig {
}
