package dada.tuda.framework.facade;

import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;

public interface FrameworkMessagingApi {
    String cancelOperation(String operationId, String reason, IMessagingEventType type);
}

