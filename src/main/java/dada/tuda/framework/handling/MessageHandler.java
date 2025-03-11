package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;

public interface MessageHandler {
    Boolean canHandle(IMessagingEventType type) throws Exception;

    Object handle(AbstractNormalMessage message) throws Exception;
}
