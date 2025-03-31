package dada.tuda.framework.configuration;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanUtilsConfig {
    @Bean
    public ApplicationContextAware applicationContext(@Autowired ApplicationContext applicationContext) throws BeansException {
        var util = new BeanUtility();
        util.setApplicationContext(applicationContext);
        return util;
    }
}

