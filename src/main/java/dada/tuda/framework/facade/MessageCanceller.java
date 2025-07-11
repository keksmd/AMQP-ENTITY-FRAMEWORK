package dada.tuda.framework.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.contexts.DomainContext;
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
    private final DomainContext domainContext;
    private final ObjectMapper objectMapper;

    public void cancelOperation(String reason, IEventAction originalAction, String operationId, IMessagingDomain domain) {
        var entity = new CancelPayload(reason);
        var action = cancelEventActionContext.getOrCreateCancelByAction(originalAction);
        NormalMessage msg = new JsonNormalMessage();
        msg.setPayloadMap(objectMapper.convertValue(entity, Map.class));
        msg.setDomainName(domain.getName());
        msg.setObjectId(operationId);
        msg.setActionTypeName(action.getName());
        sender.sendUsingTypeWithExchangeForOtherDomain(msg, domainContext.getByName(CancelPayload.CANCEL_DOMAIN));
    }

}

