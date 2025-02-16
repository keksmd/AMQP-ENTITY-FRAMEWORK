package dada.tuda.framework.normalization;

import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;


public interface NormalMessage extends Serializable {

    @Nullable
    String getObjectId();

    @Nullable
    String getActorId();

    String getOperationId();

    IMessagingEventType computeType();

    Map<String, Object> getProperties();

}
