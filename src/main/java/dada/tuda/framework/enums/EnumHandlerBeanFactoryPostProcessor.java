package dada.tuda.framework.enums;

import dada.tuda.framework.annotations.EnumBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;


public class EnumHandlerBeanFactoryPostProcessor<T extends IEnum> implements BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor, Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(EnumHandlerBeanFactoryPostProcessor.class.getName());
    public static Set<IEnum> enumBeans = new HashSet<>();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            try {
                Class<T> beanClass = (Class<T>) Class.forName(beanClassName);

                if (beanClass.isEnum() && beanClass.isAnnotationPresent(EnumBean.class)) {
                    LOG.debug("Processing ENUM class: {}", beanClass);
                    EnumBean annotation = beanClass.getAnnotation(EnumBean.class);
                    boolean hasPrefix = Boolean.parseBoolean(annotation.classnamePrefix());
                    boolean lowercase = Boolean.parseBoolean(annotation.lowercase());

                    for (T enumConst : beanClass.getEnumConstants()) {
                        String constBeanName = (hasPrefix ? (StringUtils.uncapitalize(beanClass.getSimpleName()) + ".") : "")
                                               + (lowercase ? enumConst.name().toLowerCase() : enumConst.name());

                        if (!registry.isBeanNameInUse(constBeanName)) {  // **Проверка перед регистрацией**

                            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass,
                                    () -> enumConst);
                            BeanDefinition newBeanDefinition = builder.getBeanDefinition();

                            registry.registerBeanDefinition(constBeanName, newBeanDefinition);
                            enumBeans.add(enumConst);
                            LOG.debug("Registered enum constant as bean: {}", constBeanName);
                        } else {
                            LOG.warn("Skipping already registered bean: {}", constBeanName);
                        }
                    }
                    registry.removeBeanDefinition(beanName);
                }
            } catch (ClassNotFoundException e) {
                LOG.error("Could not load class: " + beanClassName, e);
            }
        }

    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

