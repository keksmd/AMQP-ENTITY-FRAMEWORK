package dada.tuda.framework.normalization.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@EqualsAndHashCode()
@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @JsonCreator)
public class JsonNormalMessage implements NormalMessage {
    @JsonProperty("objectId")
    private @Nullable String objectId = null;
    @JsonProperty("payloadMap")
    private Map<String, Object> payloadMap;
    @JsonProperty("domainName")
    private String domainName;
    @JsonProperty("actionTypeName")
    private String actionTypeName;
    @JsonProperty("operationId")
    private String operationId;
    @JsonProperty("actorId")
    private String actorId;

    public JsonNormalMessage(@Nullable String objectId, @Nullable String actorId, @NotNull Map<String, Object> payloadMap, String domain, String actionType) {
        this.objectId = objectId;
        this.payloadMap = payloadMap;
        this.actorId = actorId;
        this.domainName = domain;
        this.actionTypeName = actionType;
        this.operationId = UUID.randomUUID().toString();
    }


    public JsonNormalMessage(@Nullable String objectId, @NotNull Map<String, Object> payloadMap, String domain, String actionType) {
        this.objectId = objectId;
        this.payloadMap = payloadMap;
        this.domainName = domain;
        this.actionTypeName = actionType;
        this.operationId = UUID.randomUUID().toString();
    }

    public JsonNormalMessage(JsonNormalMessage message) {
        this.objectId = message.objectId;
        this.payloadMap = message.payloadMap;
        this.domainName = message.domainName;
        this.actionTypeName = message.actionTypeName;
        this.operationId = message.operationId;
        this.actorId = message.actorId;
    }

}