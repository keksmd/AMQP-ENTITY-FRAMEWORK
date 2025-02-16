package dada.tuda.framework.configuration.autoconfig;


import dada.tuda.framework.configuration.aspect.publiced.CustomCacheAnnotationsConfig;
import dada.tuda.framework.configuration.aspect.publiced.CustomTracingConfig;
import dada.tuda.framework.configuration.rabbit.publiced.MessagingConfiguration;
import dada.tuda.framework.configuration.rabbit.publiced.RabbitCachingIdempotencyProviderByRedisConfiguration;
import dada.tuda.framework.configuration.rabbit.publiced.SagaConfig;
import dada.tuda.framework.configuration.redis.RedisConnectionConfig;
import dada.tuda.framework.configuration.redis.publiced.CustomCacheConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MyConfigurationSelector implements ImportSelector {

    @Override
    public String @NotNull [] selectImports(AnnotationMetadata importingClassMetadata) {
        // Получаем параметры из аннотации
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableCustomConfigs.class.getName());

        ConfigType[] configTypes = (ConfigType[]) (attributes != null ? attributes.get("value") : new ConfigType[]{});
        if (configTypes == null) {
            return new String[]{}; // Ничего не импортируем, если группы не заданы
        }
        List<ConfigType> groups = Arrays.asList(configTypes);
        List<Class<?>> result = new ArrayList<>();

        // Определяем, какие конфигурации нужно подгрузить
        if (groups.contains(ConfigType.ALL)) {
            result.addAll(List.of(
                    MessagingConfiguration.class,
                    CustomCacheConfig.class,
                    CustomTracingConfig.class,
                    RabbitCachingIdempotencyProviderByRedisConfiguration.class,
                    CustomCacheAnnotationsConfig.class,
                    SagaConfig.class));
        } else {
            if (groups.contains(ConfigType.RABBIT)) {
                result.add(MessagingConfiguration.class);
            }
            if (groups.contains(ConfigType.REDIS)) {
                result.add(RedisConnectionConfig.class);
            }
            if (groups.contains(ConfigType.IDEMPOTENCY)) {
                result.add(RabbitCachingIdempotencyProviderByRedisConfiguration.class);
            }
            if (groups.contains(ConfigType.SAGAS)) {
                result.add(SagaConfig.class);
            }
            if (groups.contains(ConfigType.CACHE)) {
                result.add(CustomCacheConfig.class);
            }
            if (groups.contains(ConfigType.TRACING)) {
                result.add(CustomTracingConfig.class);
            }
            if (groups.contains(ConfigType.CACHE_ANNOTATIONS)) {
                result.add(CustomCacheAnnotationsConfig.class);
            }
        }
        return result.stream().map(Class::getName).toArray(String[]::new); // По умолчанию ничего не подгружаем
    }
}
