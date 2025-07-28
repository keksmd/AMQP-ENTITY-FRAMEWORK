package dada.tuda.framework.normalization.messages;

import jakarta.annotation.Nullable;

import java.util.Map;


public interface NormalMessage {


    String getOperationId();

    void setOperationId(String operation);

    @Nullable
    String getObjectId();

    void setObjectId(String object);

    String getActionTypeName();

    void setActionTypeName(String actionType);

    String getDomainName();

    void setDomainName(String domain);

    @Nullable
    String getActorId();

    void setActorId(String actor);

    Map<String, Object> getPayloadMap();

    void setPayloadMap(Map<String, Object> payload);
}
