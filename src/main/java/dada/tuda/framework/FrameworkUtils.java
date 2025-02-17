package dada.tuda.framework;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class FrameworkUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();

    private FrameworkUtils() {
    }


    public static Object resolveKey(ProceedingJoinPoint joinPoint,String key) {
        Object[] args = joinPoint.getArgs();
        DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = nameDiscoverer.getParameterNames(((MethodSignature)joinPoint.getSignature()).getMethod());
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < Objects.requireNonNull(parameterNames).length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(key);
        return expression.getValue(context);


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
