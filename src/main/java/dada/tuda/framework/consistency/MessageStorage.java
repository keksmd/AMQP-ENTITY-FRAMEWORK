package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface MessageStorage {
    default void init() {
    }

    NormalMessage getByID(String operationId);

    default boolean isProcessed(NormalMessage message) {
        return isProcessedById(message.getOperationId());
    }

    boolean isProcessedById(String id);

    void storeEventAsProcessed(NormalMessage message);

    void clear();

}
