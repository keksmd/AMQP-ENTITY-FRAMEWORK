package dada.tuda.framework.configuration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanUtility implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static Object getBean(String beanId) {
        return applicationContext.getBean(beanId);
    }

    public static <T> T getBean(String beanId, Class<T> clz) {
        return applicationContext.getBean(beanId, clz);
    }

    public static <T> T getBean(Class<T> clz) {
        return applicationContext.getBean(clz);
    }

    public void setApplicationContext(@org.jetbrains.annotations.NotNull ApplicationContext applicationContext) throws BeansException {
        BeanUtility.applicationContext = applicationContext;
    }
}
