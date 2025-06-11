package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface CancelableMessageHandler<T> extends MessageHandler<T> {
    void cancel(NormalMessage message, T payload) throws Exception;
}
