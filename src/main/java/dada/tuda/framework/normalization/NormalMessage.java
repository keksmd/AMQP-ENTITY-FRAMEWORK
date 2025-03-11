package dada.tuda.framework.normalization;

import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;

import java.util.Map;


public interface NormalMessage {

    @Nullable
    String getObjectId();

    @Nullable
    String getActorId();

    String getOperationId();

    IMessagingEventType getType();

    Map<String, Object> getProperties();

}
