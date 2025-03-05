package dada.tuda.framework.configuration.aspect.publiced;

import dada.tuda.framework.redis.EnumBeanPostProcessor;
import dada.tuda.framework.redis.EnumHandlerBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration

public class EnumBeanConfiguration {
    @Bean
    static EnumHandlerBeanFactoryPostProcessor enumHandlerBeanFactoryPostProcessor(){
        return  new EnumHandlerBeanFactoryPostProcessor();
    }
    //@Bean
    static EnumBeanPostProcessor enumBeanPostProcessor(){
        return new EnumBeanPostProcessor();
    }
}