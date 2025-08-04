package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.crud.MessagingEntity;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;

@RequiredArgsConstructor
public class PayloadArgumentResolver implements RabbitHandlerArgumentResolver {
    private final PayloadConverter payloadConverter;

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAnnotationPresent(MessagingEntity.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, NormalMessage message) {
        return payloadConverter.convertPayload(message.getPayloadMap(), parameter.getParameterType());
    }
}
