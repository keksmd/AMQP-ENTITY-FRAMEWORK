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
        String actor = null;
        String object = null;
        String operation = null;
        Map<String, Object> payload = null;
        if (entity != null) {
            payload = objectMapper.convertValue(entity, Map.class);
            if (descriptor != null) {
                if (descriptor.getActorIdExtractor() != null) {
                    actor = descriptor.getActorIdExtractor().apply(entity);
                }
                if (descriptor.getObjectIdExtractor() != null) {
                    object = descriptor.getObjectIdExtractor().apply(entity);
                }
                if (descriptor.getOperationIdExtractor() != null) {
                    operation = descriptor.getOperationIdExtractor().apply(entity);
                }
                if (descriptor.getPayLoadExtractor() != null) {
                    Object extractedPayloadObject = descriptor.getPayLoadExtractor().apply(entity);
                    if (extractedPayloadObject != null) {
                        Map<String, Object> p = objectMapper.convertValue(extractedPayloadObject, Map.class);
                        if ("true".equalsIgnoreCase(descriptor.getPayloadAnnotation().replace())) {
                            payload = p;
                        } else {
                            cleanPayloadMap(payload, descriptor);
                            if ("true".equalsIgnoreCase(descriptor.getPayloadAnnotation().rewriteValues())) {
                                payload.putAll(p);
                            } else {
                                p.putAll(payload);
                                payload = p;
                            }
                        }
                    } else {
                        throw new IllegalStateException("Payload extraction from filed " + descriptor.getPayload() + "failed: " + entity);
                    }
                }
                msg.setDomain(descriptor.getDomain());
            }
        }
        operation = operation != null ? operation : operationIdGenerator.get();
        msg.setActorId(actor);
        msg.setObjectId(object);
        msg.setOperationId(operation);
        msg.setPayloadMap(payload);
        msg.setActionType(action);


        return msg;
    }

    private void cleanPayloadMap(Map<String, Object> payload, MessagingEntityDescriptor descriptor) {
        payload.remove(descriptor.getActorIdField());
        payload.remove(descriptor.getOperationIdFiled());
        payload.remove(descriptor.getObjectIdFiled());
        payload.remove(descriptor.getPayload());
    }

}
