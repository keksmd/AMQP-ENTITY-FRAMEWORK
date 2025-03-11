package dada.tuda.framework.normalization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dada.tuda.framework.normalization.converters.IMessagingEventTypeDeserializer;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class AbstractNormalMessage implements NormalMessage {
    private @Nullable String objectId;
    private  Map<String, Object> payloadMap;

    @JsonDeserialize(using = IMessagingEventTypeDeserializer.class)
    private IMessagingEventType type;

    private String operationId;
    private String actorId;


    protected AbstractNormalMessage(@Nullable String objectId, @Nullable String actorId, @NotNull Map<String, Object> payloadMap, IMessagingEventType type) {
        this.objectId = objectId;
        this.payloadMap = payloadMap;
        this.type = type;
        this.actorId = actorId;
        this.operationId = UUID.randomUUID().toString();
    }

    protected AbstractNormalMessage(@Nullable String objectId, @NotNull Map<String, Object> payloadMap, IMessagingEventType type) {
        this.objectId = objectId;
        this.payloadMap = payloadMap;
        this.type = type;
        this.operationId = UUID.randomUUID().toString();
    }

    public AbstractNormalMessage(AbstractNormalMessage message) {
        this.objectId = message.objectId;
        this.payloadMap = message.payloadMap;
        this.type = message.type;
        this.operationId = message.operationId;
        this.actorId = message.actorId;
    }

    @Override
    public Map<String, Object> getProperties() {
        return payloadMap;
    }
}