package dada.tuda.framework.configuration;

import dada.tuda.framework.redis.EnumBeanPostProcessor;
import dada.tuda.framework.redis.EnumHandlerBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EnumBeanConfiguration {
    @Bean
    static EnumHandlerBeanFactoryPostProcessor enumHandlerBeanFactoryPostProcessor(){
        return  new EnumHandlerBeanFactoryPostProcessor();
    }
    @Bean
    static EnumBeanPostProcessor enumBeanPostProcessor(){
        return new EnumBeanPostProcessor();
    }
}