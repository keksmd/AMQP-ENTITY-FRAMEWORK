package dada.tuda.framework.configuration;


import dada.tuda.framework.utils.BeanUtility;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BeanUtilsConfig {
    @Bean
    public ApplicationContextAware applicationContext(@Autowired ApplicationContext applicationContext) throws BeansException {
        var util = new BeanUtility();
        util.setApplicationContext(applicationContext);
        return util;
    }
}

