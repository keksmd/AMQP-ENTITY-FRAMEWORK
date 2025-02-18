package dada.tuda.framework.configuration.aspect.publiced;

import dada.tuda.framework.annotations.EnumBean;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class EnumBeanConfiguration {
    @Bean
    static BeanDefinitionRegistryPostProcessor enumBeanRegistrar() {
        return new EnumBeanRegistrar();
    }

    private static class EnumBeanRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {
        @Override
        public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
            Collection<URL> urls = ClasspathHelper.forJavaClassPath();
            Set<URL> filteredUrls = urls.stream()
                    // Фильтруем по каким-то условиям
                    .filter(url -> !url.getFile().contains("spring") && !url.toString().contains("hibernate-") && !url.toString().contains("jakarta") && !url.toString().contains("javax")&& !url.toString().contains("java"))
                    // Можно также проверять url.getPath() или url.toString()
                    .collect(Collectors.toSet());
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(filteredUrls)
                    .setScanners(Scanners.TypesAnnotated));Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(EnumBean.class);
            for (Class<?> clazz : annotatedClasses) {
                if (clazz.isEnum()) {
                    boolean hasPrefix;
                    String prefix = clazz.getAnnotation(EnumBean.class).enumNamePrefix();
                    if (prefix.equals("true")) {
                        hasPrefix = true;
                    } else if (prefix.equals("false")) {
                        hasPrefix = false;
                    } else {
                        throw new RuntimeException("EnumBean.enumNamePrefix must be true or false");
                    }
                    for (Object aggregate : clazz.getEnumConstants()) {
                        Enum<?> enumConst = (Enum<?>) aggregate;
                        RootBeanDefinition definition = new RootBeanDefinition();
                        definition.setTargetType(clazz);
                        definition.setInstanceSupplier(() -> enumConst);
                        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);
                        registry.registerBeanDefinition((hasPrefix ? (clazz.getSimpleName() + ".") : "") + enumConst.name(), definition);
                    }
                }
            }
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}

