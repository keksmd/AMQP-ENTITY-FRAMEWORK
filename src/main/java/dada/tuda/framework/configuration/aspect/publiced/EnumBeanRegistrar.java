package dada.tuda.framework.configuration.aspect.publiced;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;
import java.util.Set;
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class EnumBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<Class<?>> enumClasses = findAnnotatedEnums();

    }

    private Set<Class<?>> findAnnotatedEnums() throws IOException {
       //return EnumHandlerBeanFactoryPostProcessor.enumClasses.keySet();
        return null;

    }


}



