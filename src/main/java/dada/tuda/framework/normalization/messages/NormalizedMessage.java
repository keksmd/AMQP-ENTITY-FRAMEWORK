package dada.tuda.framework.normalization.messages;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

public interface NormalizedMessage extends NormalMessage {
    IEventAction getActionType();

    void setActionType(IEventAction actionType);

    IMessagingDomain getDomain();

    void setDomain(IMessagingDomain domain);
}
