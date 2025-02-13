package dada.tuda.framework;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.List;

public class FrameworkUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();

    private FrameworkUtils() {
    }

    public static Object resolveKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        // Создаём контекст для вычисления SpEL
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Добавляем аргументы метода в контекст
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // Вычисляем значение SpEL-выражения
        return parser.parseExpression(keyExpression).getValue(context);
    }

    public static boolean isReturnsList(ProceedingJoinPoint joinPoint) {
        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        boolean returnsList = List.class.isAssignableFrom(returnType);
        boolean returnsSet = List.class.isAssignableFrom(returnType);

        // Проверяем, что возвращаемое значение является наследником Collection
        if (!returnsList && !returnsSet) {
            throw new IllegalArgumentException("@CacheWithDetails return type must be of type List or Set or its subclass.");
        }
        return returnsList;
    }

    public static String extractKey(Object item, String fieldName) throws IllegalStateException {
        try {
            Field field = item.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return String.valueOf(field.get(item));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot extract key field: " + fieldName, e);
        }
    }

}
