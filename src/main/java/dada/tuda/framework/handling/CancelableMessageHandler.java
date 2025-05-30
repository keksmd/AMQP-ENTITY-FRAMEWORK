package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface CancelableMessageHandler extends MessageHandler {
    void cancel(NormalMessage message);
}
