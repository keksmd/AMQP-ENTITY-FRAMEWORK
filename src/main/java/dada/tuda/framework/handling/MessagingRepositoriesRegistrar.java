package dada.tuda.framework.handling;

import dada.tuda.framework.crud.listening.MessagingRepositoryFactoryBean;
import dada.tuda.framework.facade.MessagingEntittyRepository;
import dada.tuda.framework.normalization.types.realizations.EnableMessagingRepositories;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
public class MessagingRepositoriesRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        var allAttrs = metadata.getAllAnnotationAttributes(EnableMessagingRepositories.class.getName());
        if (allAttrs != null) {
            List<String> basePackages = new ArrayList<>();
            List<Object> raw = allAttrs.get("basePackages");
            if (raw != null && !raw.isEmpty()) {
                String[] pkgs = (String[]) raw.get(0);
                basePackages.addAll(Arrays.asList(pkgs));
            }
            if (!basePackages.isEmpty()) {
                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false, environment) {
                            @Override
                            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDef) {
                                var meta = beanDef.getMetadata();
                                return meta.isIndependent() &&
                                       (meta.isConcrete() || meta.isInterface());
                            }
                        };
                TypeFilter filter = new AssignableTypeFilter(MessagingEntittyRepository.class);
                scanner.addIncludeFilter(filter);
                scanner.setResourceLoader(resourceLoader);

                for (String basePackage : basePackages) {
                    for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                        try {
                            String className = bd.getBeanClassName();
                            Class<?> repoInterface = Class.forName(className);
                            ResolvableType rt = ResolvableType.forClass(repoInterface);
                            Class<?> entityType = rt.resolveGeneric(0);
                            String beanName = Introspector.decapitalize(repoInterface.getSimpleName());

                            GenericBeanDefinition gbd = new GenericBeanDefinition();

                            gbd.setBeanClass(MessagingRepositoryFactoryBean.class);
                            gbd.getPropertyValues().add("entityType", entityType);

                            gbd.getPropertyValues().add("repositoryInterface", repoInterface);

                            registry.registerBeanDefinition(beanName, gbd);
                        } catch (Exception e) {
                            throw new BeanDefinitionStoreException("Не удалось зарегистрировать репозиторий: " + bd, e);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
