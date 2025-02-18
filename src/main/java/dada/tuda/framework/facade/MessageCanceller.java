package dada.tuda.framework.facade;

import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.FrameworkMessageFactory;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

public class MessageCanceller {

    private  final FrameworkMessageFactory eventFabric;
    private final MessageSender sender;
    public String cancelOperation(String operationId, String reason, IMessagingEventType type) {
        AbstractNormalMessage event = eventFabric.canceled(operationId, reason, type);
       sender.sendUsingType(event, CancelUtils.getCancelActionType());
        return event.getOperationId();
    }
}

