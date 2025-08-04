package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.crud.extractor.PayloadMap;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.springframework.core.MethodParameter;

public class PayloadMapArgumentResolver implements RabbitHandlerArgumentResolver {
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PayloadMap.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, NormalMessage message) {
        return message.getPayloadMap();
    }
}
