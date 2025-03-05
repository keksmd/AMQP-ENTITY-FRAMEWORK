package dada.tuda.framework.redis;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import static dada.tuda.framework.redis.EnumHandlerBeanFactoryPostProcessor.enumBeans;

@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class EnumBeanPostProcessor implements BeanPostProcessor {

	private Set<IEnum> getEnums() {
		return enumBeans;
	}

	@SuppressWarnings("unchecked")
	private boolean isAutowiredEnumSetField(Field field) {
		if (!AnnotatedElementUtils.isAnnotated(field, Autowired.class.getName())) {
			return false;
		}

		final Class<?> fieldType = field.getType();

		if (!Set.class.isAssignableFrom(fieldType)) {
			return false;
		}

		final ParameterizedType type = (ParameterizedType) field.getGenericType();
		final Type[] typeArguments = type.getActualTypeArguments();

		final Class<? extends Type> aClass = (Class<? extends Type>) typeArguments[0];

		return aClass.isAssignableFrom(IEnum.class);
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {

		final Set<IEnum> enums = getEnums();
		if (enums.size() < 1) {
			return bean;
		}

		final Class<?> beanClass = bean.getClass();
		final Field[] fields = beanClass.getDeclaredFields();

		for (Field field : fields) {

			if (isAutowiredEnumSetField(field)) {
				field.setAccessible(true);
				ReflectionUtils.setField(field, bean, enums);
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		return bean;
	}

}
