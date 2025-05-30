package dada.tuda.framework.configuration;

import dada.tuda.framework.enums.EnumBeanPostProcessor;
import dada.tuda.framework.enums.EnumHandlerBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
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