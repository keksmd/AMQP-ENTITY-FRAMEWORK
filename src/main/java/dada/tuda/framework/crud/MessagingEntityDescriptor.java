package dada.tuda.framework.crud;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.Data;

import java.util.function.Function;

@Data
public class MessagingEntityDescriptor {
    private Function<Object, String> operationIdExtractor;
    private Function<Object, String> objectIdExtractor;
    private Function<Object, String> actorIdExtractor;
    private String operationIdFiled;
    private String objectIdFiled;
    private String actorIdField;
    private IMessagingDomain domain;

}

