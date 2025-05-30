package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface MessageStorage {
    NormalMessage getByID(String operationId);

    boolean isProcessed(NormalMessage message);

    boolean isProcessedById(String id);

    void storeEventAsProcessed(NormalMessage message);

    boolean isEnabled();
}
