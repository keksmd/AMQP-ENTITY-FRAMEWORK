package dada.tuda.framework.handling.conversion;

import dada.tuda.framework.handling.conversion.resolvers.RabbitHandlerArgumentResolver;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@RequiredArgsConstructor
public class RabbitHandlerArgumentResolverComposite {
    private final List<RabbitHandlerArgumentResolver> resolvers;

    public void addResolver(RabbitHandlerArgumentResolver resolver) {
        resolvers.add(resolver);
    }

    public Object[] resolveArguments(Method method, NormalMessage message) {
        MethodParameter[] parameters = getMethodParameters(method);
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            MethodParameter param = parameters[i];
            for (RabbitHandlerArgumentResolver resolver : resolvers) {
                if (resolver.supportsParameter(param)) {
                    args[i] = resolver.resolveArgument(param, message);
                    break;
                }
            }
        }

        return args;
    }

    private MethodParameter[] getMethodParameters(Method method) {
        Parameter[] params = method.getParameters();
        MethodParameter[] methodParameters = new MethodParameter[params.length];
        for (int i = 0; i < params.length; i++) {
            methodParameters[i] = new MethodParameter(method, i);
        }
        return methodParameters;
    }
}
