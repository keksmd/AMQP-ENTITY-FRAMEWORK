package dada.tuda.framework.configuration.aspect.publiced;

import dada.tuda.framework.annotations.EnumBean;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class EnumBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<Class<?>> enumClasses = findAnnotatedEnums();
        for (Class<?> clazz : enumClasses) {
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

    private Set<Class<?>> findAnnotatedEnums() throws IOException {
        Set<Class<?>> result = new HashSet<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        String packagePath = "classpath*:**/*.class";
        for (var resource : resolver.getResources(packagePath)) {
            try {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader.getAnnotationMetadata().hasAnnotation("dada.tuda.framework.annotations.EnumBean")) {
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
                    if (clazz.isEnum() && clazz.isAnnotationPresent(EnumBean.class)) {
                        result.add(clazz);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
            }
        }
        return result;
    }


}



