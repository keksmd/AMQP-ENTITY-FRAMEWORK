package dada.tuda.framework.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.messages.SystemMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DescriptorConverter {

    private final ObjectMapper objectMapper;
    private final OperationIdGenerator operationIdGenerator;

    public NormalizedMessage createFromDescriptor(Object entity, IEventAction action, MessagingEntityDescriptor descriptor) {
        NormalizedMessage msg = new SystemMessage();
        if (descriptor != null) {
            String actor = null;
            if (descriptor.getActorIdExtractor() != null) {
                actor = descriptor.getActorIdExtractor().apply(entity);
            }

            String object = null;
            if (descriptor.getObjectIdExtractor() != null) {
                object = descriptor.getObjectIdExtractor().apply(entity);
            }

            String operation = null;
            if (descriptor.getOperationIdExtractor() != null) {
                operation = descriptor.getOperationIdExtractor().apply(entity);
            }
            operation = operation != null ? operation : operationIdGenerator.get();
            msg.setActorId(actor);
            msg.setObjectId(object);
            msg.setOperationId(operation);
            msg.setDomain(descriptor.getDomain());
        }
        Map<String, Object> payload = objectMapper.convertValue(entity, Map.class);
        payload.remove("actorId");
        payload.remove("operationId");
        payload.remove("objectId");
        msg.setPayloadMap(payload);
        msg.setActionType(action);
        return msg;
    }

}
