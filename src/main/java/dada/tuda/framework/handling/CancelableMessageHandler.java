package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.AbstractNormalMessage;

public interface CancelableMessageHandler extends MessageHandler {
    void cancel(AbstractNormalMessage message);
}
