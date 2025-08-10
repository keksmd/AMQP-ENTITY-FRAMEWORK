package dada.tuda.framework.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MessageCanceller {

    private final IEventActionContext cancelEventActionContext;
    private final MessageSender sender;
    private final ObjectMapper objectMapper;

    public void cancelOperation(String reason, IEventAction originalAction, String operationId, IMessagingDomain domain) {
        var entity = new CancelPayload(reason);
        NormalMessage msg = new JsonNormalMessage();
        msg.setPayloadMap(objectMapper.convertValue(entity, Map.class));
        msg.setDomainName(domain.getName());
        msg.setObjectId(operationId);
        msg.setActionTypeName(cancelEventActionContext.getOrCreateCancelByAction(originalAction).getName());
        sender.sendUsingType(msg);
    }

}

