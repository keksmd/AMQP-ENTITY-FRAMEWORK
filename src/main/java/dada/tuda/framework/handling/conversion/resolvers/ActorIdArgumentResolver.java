package dada.tuda.framework.handling.conversion.resolvers;

import dada.tuda.framework.crud.extractor.ActorId;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.springframework.core.MethodParameter;

public class ActorIdArgumentResolver implements RabbitHandlerArgumentResolver {
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ActorId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, NormalMessage message) {
        return message.getActorId();
    }
}
