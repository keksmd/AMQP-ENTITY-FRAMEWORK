package dada.tuda.framework;

import dada.tuda.framework.facade.MessagingEntittyRepository;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.List;
import java.util.Map;


public class MessagingRepositoriesMissingAnnotationChecker
        implements SmartInitializingSingleton, ApplicationContextAware, EnvironmentAware {

    private ApplicationContext ctx;
    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 1) Смотрим, есть ли в контексте реально зарегистрированные MessagingEntittyRepository-бины
        Map<String, MessagingEntittyRepository> reposInContext =
                ctx.getBeansOfType(MessagingEntittyRepository.class);
        if (!reposInContext.isEmpty()) {
            // Всё ок, хоть один бин есть
            return;
        }

        // 2) Узнаём автосканируемые пакеты приложения (@SpringBootApplication)
        List<String> basePackages = AutoConfigurationPackages.get(ctx);

        // 3) Сканируем classpath на наличие интерфейсов, наследующих MessagingEntittyRepository
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.addIncludeFilter(new AssignableTypeFilter(MessagingEntittyRepository.class));

        boolean foundInterface = false;
        for (String pkg : basePackages) {
            if (!scanner.findCandidateComponents(pkg).isEmpty()) {
                foundInterface = true;
                break;
            }
        }

        // 4) Если интерфейсы есть, а бин-проксей нет — пользователь забыл @EnableMessagingRepositories
        if (foundInterface) {
            throw new IllegalStateException(
                    """
                            === Messaging Starter error ===
                            Найдены интерфейсы MessagingEntittyRepository в вашем коде, но ни один
                            прокси-бин не был зарегистрирован. Пожалуйста, добавьте над вашим
                            @Configuration или @SpringBootApplication классом аннотацию:
                            
                                @EnableMessagingRepositories(basePackages = "…ваш.пакет…")
                            
                            ================================
                            """
            );
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}