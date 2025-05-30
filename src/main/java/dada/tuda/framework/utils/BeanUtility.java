package dada.tuda.framework.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanUtility implements ApplicationContextAware {

    private static final Singleton INSTANCE = new Singleton();

    public static Object getBean(String beanId) {
        return INSTANCE.applicationContext.getBean(beanId);
    }

    public static <T> T getBean(String beanId, Class<T> clz) {
        return INSTANCE.applicationContext.getBean(beanId, clz);
    }

    public static <T> T getBean(Class<T> clz) {
        return INSTANCE.applicationContext.getBean(clz);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanUtility.INSTANCE.setApplicationContext(applicationContext);
    }

    @Setter
    @Getter
    static class Singleton {
        private ApplicationContext applicationContext;
    }
}
