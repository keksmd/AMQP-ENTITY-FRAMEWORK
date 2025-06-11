package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalizedMessage;

public interface MessageHandler<T> {
    boolean canHandle(NormalizedMessage message);

    Object handle(NormalizedMessage message, T payload) throws Exception;
}
