package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.normalization.messages.NormalMessage;
import org.springframework.core.MethodParameter;

public class NormalMessageArgumentResolver implements RabbitHandlerArgumentResolver {


    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(NormalMessage.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, NormalMessage message) {
        return message;
    }
}
