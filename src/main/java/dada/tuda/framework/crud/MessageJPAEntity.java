package dada.tuda.framework.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import dada.tuda.framework.normalization.converters.MapToJsonConverter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.util.ProxyUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RedisHash(value = "event")
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class MessageJPAEntity implements Serializable, NormalMessage {
    @JsonProperty("objectId")
    private @Nullable String objectId;
    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    @JsonProperty("payloadMap")
    private Map<String, Object> payloadMap;
    @JsonProperty("actionTypeName")
    private String actionTypeName;
    @JsonProperty("domainName")
    private String domainName;
    @org.springframework.data.annotation.Id
    @jakarta.persistence.Id
    @JsonProperty("operationId")
    private String operationId;
    @JsonProperty("actorId")
    private String actorId;
    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long ttl;


    @Id
    @org.springframework.data.annotation.Id
    public String getId() {
        return this.getOperationId();
    }

    @Override
    public final int hashCode() {
        return ProxyUtils.getUserClass(this).hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ProxyUtils.getUserClass(this) != ProxyUtils.getUserClass(o)) return false;
        MessageJPAEntity that = (MessageJPAEntity) o;
        return getOperationId() != null && Objects.equals(getOperationId(), that.getOperationId());
    }
}
