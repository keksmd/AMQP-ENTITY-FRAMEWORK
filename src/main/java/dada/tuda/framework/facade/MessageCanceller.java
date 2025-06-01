package dada.tuda.framework.facade;

import dada.tuda.framework.crud.contexts.CancelEventActionContext;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageCanceller {

    private final MessagingEntittyRepository<CancelPayload> repository;
    private final CancelEventActionContext eventActionContext;

    public void cancelOperation(String reason, IEventAction originalAction, String operationId) {
        var cancel = new CancelPayload(reason, operationId);
        repository.doAction(cancel, eventActionContext.getOrCreateCancelByAction(originalAction));
    }

}

