package dada.tuda.framework.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.ICancelEventActionContext;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.messages.SystemMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MessageCanceller {

    private final ICancelEventActionContext cancelEventActionContext;
    private final MessageSender sender;
    private final ObjectMapper objectMapper;

    public void cancelOperation(String reason, IEventAction originalAction, String operationId, IMessagingDomain domain) {
        var entity = new CancelPayload(reason);
        var action = cancelEventActionContext.getOrCreateCancelByAction(originalAction);
        NormalizedMessage msg = new SystemMessage();
        msg.setPayloadMap(objectMapper.convertValue(entity, Map.class));
        msg.setDomain(domain);
        msg.setObjectId(operationId);
        msg.setActionType(action);
        sender.sendUsingType(msg);
    }

}

