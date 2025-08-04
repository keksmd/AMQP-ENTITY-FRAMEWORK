package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.normalization.messages.NormalMessage;
import org.springframework.core.MethodParameter;

public interface RabbitHandlerArgumentResolver {
    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter, NormalMessage message);
}
