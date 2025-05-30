package dada.tuda.framework.crud.extractor;

import java.util.UUID;

public class UUUDOperationIdGenerator implements OperationIdGenerator {
    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
}
