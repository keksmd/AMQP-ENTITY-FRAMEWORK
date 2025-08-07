package dada.tuda.framework.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.crud.extractor.OperationIdGenerator;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DescriptorConverter {

    private final ObjectMapper objectMapper;
    @Setter
    private OperationIdGenerator operationIdGenerator;


    public DescriptorConverter(ObjectMapper objectMapper, OperationIdGenerator operationIdGenerator) {
        this.objectMapper = objectMapper;
        this.operationIdGenerator = operationIdGenerator;
    }

    public NormalMessage createFromDescriptor(Object entity, IEventAction action, MessagingEntityDescriptor descriptor) {
        NormalMessage msg = new JsonNormalMessage();
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
                        log.warn("Payload extraction for class {} from filed {} failed: {}", entity.getClass(), descriptor.getPayload(), entity);
                        payload = null;
                    }
                }
                msg.setDomainName(descriptor.getDomain().getName());
            } else {
                throw new IllegalStateException("Descriptor is null for entity: " + entity);
            }
        }
        operation = operation != null ? operation : operationIdGenerator.get();
        msg.setActorId(actor);
        msg.setObjectId(object);
        msg.setOperationId(operation);
        msg.setPayloadMap(payload);
        msg.setActionTypeName(action.getName());


        return msg;
    }

    private void cleanPayloadMap(Map<String, Object> payload, MessagingEntityDescriptor descriptor) {
        payload.remove(descriptor.getActorIdField());
        payload.remove(descriptor.getOperationIdFiled());
        payload.remove(descriptor.getObjectIdFiled());
        payload.remove(descriptor.getPayload());
    }

}
