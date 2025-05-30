package dada.tuda.framework.facade;

import dada.tuda.framework.repositories.cancel.CancelPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

public class MessageCanceller {

    private final MessagingEntittyRepository<CancelPayload> repository;

    public void cancelOperation(String reason, String operationId) {
        var cancel = new CancelPayload(reason, operationId);
        repository.create(cancel);
    }

}

