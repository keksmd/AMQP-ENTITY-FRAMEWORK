package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.crud.extractor.OperationId;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.springframework.core.MethodParameter;

public class OperationIdArgumentResolver implements RabbitHandlerArgumentResolver {
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OperationId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, NormalMessage message) {
        return message.getOperationId();
    }
}
