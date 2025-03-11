package dada.tuda.framework.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dada.tuda.framework.normalization.NormalMessage;
import dada.tuda.framework.normalization.converters.EventTypeConverter;
import dada.tuda.framework.normalization.converters.IMessagingEventTypeDeserializer;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Map;

@RedisHash(value = "event")
@Getter
@Setter
@ToString
@Entity
@EqualsAndHashCode()
public class EventEntity implements Serializable, NormalMessage {
    private @Nullable String objectId;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> payloadMap;
    @Convert(converter = EventTypeConverter.class)
    @JsonDeserialize(using = IMessagingEventTypeDeserializer.class)
    private IMessagingEventType type;
    @org.springframework.data.annotation.Id
    @jakarta.persistence.Id
    private String operationId;
    private String actorId;

    public EventEntity() {
    }
    public EventEntity(NormalMessage type) {
        this.objectId = type.getObjectId();
        this.type = type.getType();
        this.operationId = type.getOperationId();
        this.actorId = type.getActorId();

    }



    @Id
    @org.springframework.data.annotation.Id
    public String getId() {
        return this.getOperationId();
    }


    @Override
    public Map<String, Object> getProperties() {
        return payloadMap;
    }
}
