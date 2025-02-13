package dada.tuda.framework.handling;

import dada.tuda.framework.ex.EventHandlingException;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractQueryMessageHandler<T> implements MessageHandler {

    @Override
    public Object handle(AbstractNormalMessage message) throws EventHandlingException {
        return handleQuery(message);
    }

    public abstract T handleQuery(AbstractNormalMessage message) throws EventHandlingException;
}
