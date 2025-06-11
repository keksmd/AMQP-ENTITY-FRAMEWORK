package dada.tuda.framework.normalization.messages;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
public class SystemMessage implements NormalizedMessage {
    Map<String, Object> payloadMap;
    private String objectId;
    private String operationId;
    private IMessagingDomain domain;
    private IEventAction actionType;
    private String actorId;

    @Override
    public String getActionTypeName() {
        return actionType.getName();
    }

    @Override
    public void setActionTypeName(String actionType) {
        log.warn("Trying  to set an action type name for SystemMessage\nActual eventType: {}, name did no set: {}", this.getActionType(), actionType);
    }

    @Override
    public String getDomainName() {
        return domain.getName();
    }

    @Override
    public void setDomainName(String domain) {
        log.warn("Trying  to set an domain name for SystemMessage\nActual domain: {}, name did no set: {}", this.getDomain(), domain);
    }
}
