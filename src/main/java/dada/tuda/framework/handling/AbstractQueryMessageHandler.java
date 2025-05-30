package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractQueryMessageHandler<T> implements MessageHandler {

    @Override
    public Object handle(NormalizedMessage message) throws Exception {
        return handleQuery(message);
    }

    public abstract T handleQuery(NormalMessage message) throws Exception;
}
