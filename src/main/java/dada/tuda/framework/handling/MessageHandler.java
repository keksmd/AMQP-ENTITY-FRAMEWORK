package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

public interface MessageHandler {
    Boolean canHandle(IMessagingDomain domain, IEventAction action);

    Object handle(NormalizedMessage message) throws Exception;
}
