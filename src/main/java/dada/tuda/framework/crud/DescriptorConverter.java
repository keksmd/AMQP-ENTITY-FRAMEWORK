package dada.tuda.framework.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.messages.SystemMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.Setter;

import java.util.Map;


public class DescriptorConverter {

    private final ObjectMapper objectMapper;
    @Setter
    private OperationIdGenerator operationIdGenerator;


    public DescriptorConverter(ObjectMapper objectMapper, OperationIdGenerator operationIdGenerator) {
        this.objectMapper = objectMapper;
        this.operationIdGenerator = operationIdGenerator;
    }

    public NormalizedMessage createFromDescriptor(Object entity, IEventAction action, MessagingEntityDescriptor descriptor) {
        NormalizedMessage msg = new SystemMessage();

        Map<String, Object> payload = objectMapper.convertValue(entity, Map.class);
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
            payload.remove(descriptor.getActorIdField());
            payload.remove(descriptor.getOperationIdFiled());
            payload.remove(descriptor.getObjectIdFiled());
        }
        msg.setPayloadMap(payload);
        msg.setActionType(action);

        return msg;
    }

}
