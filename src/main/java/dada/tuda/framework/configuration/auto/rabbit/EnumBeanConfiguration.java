package dada.tuda.framework.configuration.auto.rabbit;

import dada.tuda.framework.enums.EnumBeanPostProcessor;
import dada.tuda.framework.enums.EnumHandlerBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EnumBeanConfiguration {
    @Bean
    static EnumHandlerBeanFactoryPostProcessor enumHandlerBeanFactoryPostProcessor() {
        return new EnumHandlerBeanFactoryPostProcessor();
    }

    @Bean
    static EnumBeanPostProcessor enumBeanPostProcessor() {
        return new EnumBeanPostProcessor();
    }
}