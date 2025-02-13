package dada.tuda.framework.consistency;

public interface IdempotencyProvider {

    boolean eventProcessed(String operationId);

    void storeEventAsProcessed(String operationId);
}
