package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.messages.NormalMessage;

import java.util.Collection;

public interface MessageStorage {
    Collection<? extends NormalMessage> getMessages(String domainName);
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
