package dada.tuda.framework.conf.beans;

import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.handling.ActionHandler;
import dada.tuda.framework.handling.DomainHandlers;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@DomainHandlers(domain = "example")
public class MyHandler {
    private final MessageStorage messageStorage;

    @ActionHandler(action = "created", cancelMethod = "cancelHandle")
    public void handle(NormalizedMessage message) throws Exception {
        if (message.getObjectId() == null) {
            throw new IllegalStateException("object id is null");
        }
    }

    @ActionHandler(action = "deleted")
    public void delete(NormalizedMessage message) throws Exception {
        if (message.getObjectId() == null) {
            throw new IllegalStateException("object id is null");
        }
    }

    public void cancelHandle(NormalizedMessage message, TestEntity entity) throws Exception {
        JsonNormalMessage jsonMessage = new JsonNormalMessage();
        jsonMessage.setPayloadMap(Map.of("CANCELLATION", entity.getId()));
        jsonMessage.setOperationId(message.getOperationId() + "-cancel");
        jsonMessage.setDomainName("example");
        messageStorage.storeEventAsProcessed(jsonMessage);
    }

    @ActionHandler(action = "requested")
    public Object handleQuery(NormalizedMessage message) throws Exception {
        TestEntity ans = new TestEntity();
        ans.setId(message.getObjectId());
        return ans;
    }
}
