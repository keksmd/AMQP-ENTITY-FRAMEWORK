package dada.tuda.framework.entity;

import dada.tuda.framework.normalization.AbstractNormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
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
@EqualsAndHashCode(callSuper = true)
public class EventEntity extends AbstractNormalMessage implements Serializable {
    public EventEntity() {
        super();
    }

    public EventEntity(AbstractNormalMessage abstractNormalMessage) {
        super(abstractNormalMessage);
    }

    public EventEntity(String objectId, String actorId, Map<String, Object> payloadMap, IMessagingEventType type) {
        super(objectId, actorId, payloadMap, type);
    }

    public EventEntity(String objectId, Map<String, Object> payloadMap, IMessagingEventType type) {
        super(objectId, payloadMap, type);
    }

    @Id
    @org.springframework.data.annotation.Id
    public String getId() {
        return this.getOperationId();
    }


}
