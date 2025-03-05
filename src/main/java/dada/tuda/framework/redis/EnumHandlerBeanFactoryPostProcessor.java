package dada.tuda.framework.redis;

import dada.tuda.framework.annotations.EnumBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)

public class EnumHandlerBeanFactoryPostProcessor<T extends  IEnum> implements BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor,Ordered  {
    private static final Logger LOG = LoggerFactory.getLogger(EnumHandlerBeanFactoryPostProcessor.class.getName());

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		String[] beanNames = registry.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			BeanDefinition beanDefinition =registry.getBeanDefinition(beanName);
			String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName == null) {
				continue;
			}
			try {
				Class<T> beanClass = (Class<T>) Class.forName(beanClassName);

				if (beanClass.isEnum() && beanClass.isAnnotationPresent(EnumBean.class)) {
					beanDefinition.setFactoryMethodName("values");
					LOG.debug("Processing ENUM class: {}", beanClass);
					EnumBean annotation = beanClass.getAnnotation(EnumBean.class);
					boolean hasPrefix = Boolean.parseBoolean(annotation.enumNamePrefix());
					for (T enumConst : beanClass.getEnumConstants()) {
						String constBeanName = (hasPrefix ? (StringUtils.uncapitalize(beanClass.getSimpleName()) + ".") : "") + enumConst.name();
						if (!registry.isBeanNameInUse(constBeanName)) {  // **Проверка перед регистрацией**
							BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass,
									() -> enumConst);
							BeanDefinition newBeanDefinition = builder.getBeanDefinition();
							registry.registerBeanDefinition(constBeanName, newBeanDefinition);
							LOG.debug("Registered enum constant as bean: {}", constBeanName);
						} else {
							LOG.warn("Skipping already registered bean: {}", constBeanName);
						}
					}
				}
			} catch (ClassNotFoundException e) {
				LOG.error("Could not load class: " + beanClassName, e);
			}
		}

	}


	@Override
		public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
			String[] beanNames = beanFactory.getBeanDefinitionNames();

			for (String beanName : beanNames) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				String beanClassName = beanDefinition.getBeanClassName();
				if (beanClassName == null) {
					continue;
				}
				try {
					Class<?> beanClass = Class.forName(beanClassName);

					if (beanClass.isEnum() && beanClass.isAnnotationPresent(EnumBean.class)) {

						LOG.debug("Processing ENUM class: {}", beanClass);

						EnumBean annotation = beanClass.getAnnotation(EnumBean.class);
						boolean hasPrefix = Boolean.parseBoolean(annotation.enumNamePrefix());

						for (Object aggregate : beanClass.getEnumConstants()) {
							Enum<?> enumConst = (Enum<?>) aggregate;
							String constBeanName = (hasPrefix ? (StringUtils.uncapitalize(beanClass.getSimpleName()) + ".") : "") + enumConst.name();
							if (!beanFactory.containsBean(constBeanName)) {  // **Проверка перед регистрацией**
								//beanFactory.registerSingleton(constBeanName, enumConst);
								LOG.debug("Registered enum constant as bean: {}", constBeanName);
							} else {
								LOG.warn("Skipping already registered bean: {}", constBeanName);
							}
						}
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

