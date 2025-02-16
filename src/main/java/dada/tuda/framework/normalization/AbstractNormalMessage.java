package dada.tuda.framework.normalization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dada.tuda.framework.normalization.converters.EventTypeConverter;
import dada.tuda.framework.normalization.converters.IMessagingEventTypeDeserializer;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.Map;
import java.util.UUID;

@MappedSuperclass
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class AbstractNormalMessage implements NormalMessage {
    private @Nullable String objectId;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> payloadMap;
    @Convert(converter = EventTypeConverter.class)
    @JsonDeserialize(using = IMessagingEventTypeDeserializer.class)
    private IMessagingEventType type;
    @Id
    @jakarta.persistence.Id
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
    public @Nullable String getObjectId() {
        return objectId;
    }

    @Override
    public @Nullable String getActorId() {
        return actorId;
    }

    @Override
    public String getOperationId() {
        return operationId;
    }

    @Override
    public Map<String, Object> getProperties() {
        return payloadMap;
    }

    @Override
    public IMessagingEventType computeType() {
        return type;
    }


}